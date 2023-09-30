package gui.contact;

import gui.bootup.SplashScreen;
import gui.chat.ChatGUI;
import gui.dialog.Error;
import gui.dialog.RefusedDialog;
import gui.dialog.RequestingDialog;
import logic.data.*;
import logic.manager.ContactManager;
import logic.manager.PeerManager;
import logic.network.*;
import logic.storage.ContactDataBase;
import logic.storage.DataBase;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.*;

public class ContactGUI {
    private boolean isConnected;
    private final User user;
    private ScheduledExecutorService broadcastTask;

    private JPanel contactPanel;
    private JPanel topBarPanel;
    private JPanel contactListPanel;
    private JPanel peerListPanel;
    private JLabel topBarLogo;
    private JLabel contactTitle;
    private JLabel peerTitle;
    private JLabel contactInstructionLabel;
    private JLabel peerInstructionLabel;
    private JLabel debug;
    private JButton button1;

    public ContactGUI() {
        // Set up some ui stuff
        topBarPanel.setBackground(Constant.MAIN_ACCENT_COLOR);
        contactListPanel.setBackground(Color.WHITE);
        peerListPanel.setBackground(Color.WHITE);

        // Get user from boot up
        this.user = SplashScreen.user;

        // TODO: add some loading animation

        // init Handlers and Managers
        initManager();

        // Init the logic
        initThread();

        // Display initial contacts and peers
        displayContacts();
        displayPeers();

        try {
            debug.setText(Address.getLocalIp() + " @ " + Address.getInterface());
            button1.addActionListener(e -> {
               changeToChat();
            });
        } catch (SocketException e) {
            Error dialog = new Error(SplashScreen.frame, e);
            dialog.display();
        }
    }

    private void initManager() {
        try {
            // Load contact if null
            if (SplashScreen.contactManager == null) {
                initContactManager();
            }

            // Set up peer if null
            if (SplashScreen.peerManager == null) {
                SplashScreen.peerManager = new PeerManager();
            }

            // Chat boot-up // TODO: test this
            if (SplashScreen.chatSender == null || SplashScreen.chatReceiver == null) {
                SplashScreen.chatSender = new ChatSender(user, DataBase.getDataBasePath(user.getId()));
                SplashScreen.chatReceiver = new ChatReceiver(user, SplashScreen.contactManager, DataBase.getDataBasePath(user.getId()),
                        SplashScreen.frame);
            }
        } catch (IOException e) {
            Error dialog = new Error(SplashScreen.frame, e);
            dialog.display();
        }
    }

    private void initThread() {
        // Start broadcasting own detail
        broadcastTask = Executors.newSingleThreadScheduledExecutor();
        broadcastTask.scheduleAtFixedRate(() -> {
            try {
                Broadcast.broadcast(user.getId(), user.getUserName());
            } catch (IOException ex) {
                Error dialog = new Error(SplashScreen.frame, ex);
                dialog.display();
            }
        }, 0, 5, TimeUnit.SECONDS);

        isConnected = false;
        if (SplashScreen.broadcastListenerThread != null &&
                SplashScreen.exchangeListenerThread != null &&
                SplashScreen.chatListenerThread != null) {
            return;
        }

        // Listen for other broadcast
        SplashScreen.broadcastListenerThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    if (isConnected) continue;

                    ManagersWrapper managersWrapper = Broadcast.listenForBroadcast(user.getId(),
                            SplashScreen.contactManager, SplashScreen.peerManager);

                    if (managersWrapper == null) continue;
                    SplashScreen.contactManager = managersWrapper.contactManager();
                    SplashScreen.peerManager = managersWrapper.peerManager();

                    updateList();
                }
            } catch (IOException e) {
                Error dialog = new Error(SplashScreen.frame, e);
                dialog.display();
            }
        });

        // Listen for contact exchange request
        SplashScreen.exchangeListenerThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    if (isConnected) continue;

                    ContactManager gotContactManager = Exchange.listener(user, SplashScreen.contactManager,
                            DataBase.getDataBasePath(user.getId()), SplashScreen.frame);

                    if (gotContactManager == null) continue;
                    SplashScreen.contactManager = gotContactManager;
                    SplashScreen.peerManager.removePeer(SplashScreen.contactManager.getLatestAddedPeer());

                    updateList();
                }
            } catch (NoSuchPaddingException | IllegalBlockSizeException | IOException | NoSuchAlgorithmException |
                     BadPaddingException | InvalidKeyException | InterruptedException | InvalidKeySpecException |
                     InvalidAlgorithmParameterException | SQLException ex) {
                Error dialog = new Error(SplashScreen.frame, ex);
                dialog.display();
            }
        });

        // Chat receiver listener if accepted change to chat interface
        SplashScreen.chatListenerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (isConnected) continue;

                if (SplashScreen.chatReceiver.receiverHandshakeListener()) {
                    changeToChat();
                }
            }
        });

        // Start the treads
        SplashScreen.broadcastListenerThread.start();
        SplashScreen.exchangeListenerThread.start();
        SplashScreen.chatListenerThread.start();
    }

    private void updateList() {
        peerListPanel.removeAll();
        contactListPanel.removeAll();
        displayPeers();
        displayContacts();
        peerListPanel.revalidate();
        contactListPanel.revalidate();
        peerListPanel.repaint();
        contactListPanel.repaint();
    }

    private void displayPeers() {
        JList<Peer> peerList = new JList<>(new Vector<>(SplashScreen.peerManager.getPeers()));
        peerList.setCellRenderer(new PeerCellRenderer());

        peerList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = peerList.locationToIndex(e.getPoint());
                    Peer peer = peerList.getModel().getElementAt(index);

                    RequestingDialog dialog = new RequestingDialog(SplashScreen.frame, "Requesting");
                    dialog.display();

                    new SwingWorker<Boolean, Void>() {
                        @Override
                        protected Boolean doInBackground() {
                            boolean accepted = false;
                            try {
                                accepted = Exchange.knowEachOther(user, peer, SplashScreen.contactManager, DataBase.getDataBasePath(user.getId()));
                            } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException |
                                     IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
                                     InvalidKeySpecException | InvalidAlgorithmParameterException | SQLException ex) {
                                Error dialog = new Error(SplashScreen.frame, ex);
                                dialog.display();
                            }
                            return accepted;
                        }

                        @Override
                        protected void done() {
                            try {
                                boolean accepted = get();
                                dialog.close();

                                if (accepted) {
                                    SplashScreen.peerManager.removePeer(peer);
                                    updateList();
                                } else {
                                    new RefusedDialog(SplashScreen.frame, "Refused or timed out").display();
                                }
                            } catch (ExecutionException | InterruptedException ex) {
                                Error dialog = new Error(SplashScreen.frame, ex);
                                dialog.display();
                            }
                        }
                    }.execute();
                }
            }
        });

        JScrollPane peerScrollPane = new JScrollPane(peerList);
        peerScrollPane.setBorder(null);
        peerListPanel.add(peerScrollPane);
    }

    private void displayContacts() {
        JList<Contact> contactList = new JList<>(new Vector<>(SplashScreen.contactManager.getContacts()));
        contactList.setCellRenderer(new ContactCellRenderer());

        contactList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = contactList.locationToIndex(e.getPoint());
                    Contact contact = contactList.getModel().getElementAt(index);

                    RequestingDialog dialog = new RequestingDialog(SplashScreen.frame, "Requesting");
                    dialog.display();

                    new SwingWorker<Boolean, Void>() {
                        @Override
                        protected Boolean doInBackground() throws Exception {
                            boolean accepted;
                            accepted = SplashScreen.chatSender.connect(contact);
                            return accepted;
                        }

                        @Override
                        protected void done() {
                            try {
                                boolean accepted = get();
                                dialog.close();

                                if (accepted) {
                                    changeToChat();
                                } else {
                                    new RefusedDialog(SplashScreen.frame, "Refused or timed out").display();
                                }
                            } catch (ExecutionException e) {
                                dialog.close();
                                new RefusedDialog(SplashScreen.frame, "Refused or timed out").display();
                            } catch (InterruptedException ex) {
                                Error dialog = new Error(SplashScreen.frame, ex);
                                dialog.display();
                            }
                        }
                    }.execute();
                }
            }
        });

        JScrollPane contactScrollPane = new JScrollPane(contactList);
        contactScrollPane.setBorder(null);
        contactListPanel.add(contactScrollPane);
    }

    private void initContactManager() throws IOException {
        String database = DataBase.getDataBasePath(user.getId());

        try {
            ContactDataBase.initialization(database);
            SplashScreen.contactManager = new ContactManager();
            SplashScreen.contactManager.addContact(ContactDataBase.getContactFromDatabase(user, database));
        } catch (SQLException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                 IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException |
                 InvalidKeySpecException ex) {
            Error dialog = new Error(SplashScreen.frame, ex);
            dialog.display();
        }
    }

    private void changeToChat() {
        broadcastTask.shutdown();
        isConnected = true;

        SplashScreen.changePanel(ChatGUI.getChatPanel());
    }

    public static JPanel getContact() {
        return new ContactGUI().contactPanel;
    }

    private void createUIComponents() {
        ImageIcon logoIcon = new ImageIcon(Objects.requireNonNull(ContactGUI.class.getResource("/CyChatReversed.png")));
        topBarLogo = new JLabel(logoIcon);
    }
}
