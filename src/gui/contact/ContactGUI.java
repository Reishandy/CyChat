package gui.contact;

import gui.bootup.SplashScreen;
import gui.chat.ChatGUI;
import gui.dialog.Error;
import gui.dialog.RefusedDialog;
import gui.dialog.RequestingDialog;
import logic.data.Contact;
import logic.data.Peer;
import logic.data.User;
import logic.manager.ContactManager;
import logic.data.ManagersWrapper;
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
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

public class ContactGUI {
    public static ContactManager contactManager;
    public static PeerManager peerManager;
    public static ChatSender chatSender;
    public static ChatReceiver chatReceiver;
    private final User user;
    private Thread broadcastThread;
    private Thread broadcastListenerThread;
    private Thread exchangeListenerThread;
    private Thread chatListenerThread;

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

    public ContactGUI() {
        // Set up some ui stuff
        topBarPanel.setBackground(new Color(140, 82, 255));
        contactListPanel.setBackground(Color.WHITE);
        peerListPanel.setBackground(Color.WHITE);

        // Get user from boot up
        this.user = SplashScreen.user;

        // TODO: add some loading animation

        // Init the logic
        init();

        // Display initial contacts and peers
        displayContacts();
        displayPeers();

        try {
            debug.setText(Address.getLocalIp() + " @ " + Address.getInterface());
        } catch (SocketException e) {
            Error dialog = new Error(SplashScreen.frame, e);
            dialog.display();
        }
    }

    private void init() {
        try {
            // Load contact and set up peer
            initContactManager();
            peerManager = new PeerManager();

            // Chat boot-up // TODO: test this
            chatSender = new ChatSender(user, DataBase.getDataBasePath(user.getId()));
            chatReceiver = new ChatReceiver(user, contactManager, DataBase.getDataBasePath(user.getId()), SplashScreen.frame);
        } catch (IOException e) {
            Error dialog = new Error(SplashScreen.frame, e);
            dialog.display();
        }

        // Broadcast own detail
        broadcastThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Broadcast.broadcast(user.getId(), user.getUserName());
                    Thread.sleep(5000);
                } catch (InterruptedException | IOException ex) {
                    Error dialog = new Error(SplashScreen.frame, ex);
                    dialog.display();
                }
            }
        });

        // Listen for other broadcast
        broadcastListenerThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    ManagersWrapper managersWrapper = Broadcast.listenForBroadcast(user.getId(), contactManager, peerManager);

                    if (managersWrapper == null) continue;
                    contactManager = managersWrapper.contactManager();
                    peerManager = managersWrapper.peerManager();

                    updateList();
                }
            } catch (IOException e) {
                Error dialog = new Error(SplashScreen.frame, e);
                dialog.display();
            }

        });

        // Listen for contact exchange request
        exchangeListenerThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    ContactManager gotContactManager = Exchange.listener(user, contactManager, SplashScreen.frame);

                    if (gotContactManager == null) continue;
                    contactManager = gotContactManager;
                    peerManager.removePeer(contactManager.getLatestAddedPeer());

                    updateList();
                }
            } catch (NoSuchPaddingException | IllegalBlockSizeException | IOException | NoSuchAlgorithmException |
                     BadPaddingException | InvalidKeyException | InterruptedException | InvalidKeySpecException ex) {
                Error dialog = new Error(SplashScreen.frame, ex);
                dialog.display();
            }
        });

        // Chat receiver listener if accepted change to chat interface
        // TODO Fix this and maybe use some manual CLI sender?
        chatListenerThread = new Thread(() -> {
            while (true) {
                if (chatReceiver.getIsConnected()) {
                    // Stop the broadcast and exchange thread
                    broadcastThread.interrupt();
                    broadcastListenerThread.interrupt();
                    exchangeListenerThread.interrupt();
                    chatListenerThread.interrupt();

                    SplashScreen.changePanel(ChatGUI.getChatPanel());
                }
            }
        });

        // Start the treads
        broadcastThread.start();
        broadcastListenerThread.start();
        exchangeListenerThread.start();
        chatListenerThread.start();
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
        JList<Peer> peerList = new JList<>(new Vector<>(peerManager.getPeers()));
        peerList.setCellRenderer(new PeerCellRenderer());

        peerList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = peerList.locationToIndex(e.getPoint());
                    Peer peer = peerList.getModel().getElementAt(index);

                    RequestingDialog dialog = new RequestingDialog(SplashScreen.frame);
                    dialog.display();

                    new SwingWorker<Boolean, Void>() {
                        @Override
                        protected Boolean doInBackground() {
                            boolean accepted = false;
                            try {
                                accepted = Exchange.knowEachOther(user, peer, contactManager, DataBase.getDataBasePath(user.getId()));
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
                                    peerManager.removePeer(peer);
                                    updateList();
                                } else {
                                    new RefusedDialog(SplashScreen.frame).display();
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
        JList<Contact> contactList = new JList<>(new Vector<>(contactManager.getContacts()));
        contactList.setCellRenderer(new ContactCellRenderer());

        contactList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = contactList.locationToIndex(e.getPoint());
                    Contact contact = contactList.getModel().getElementAt(index);

                    RequestingDialog dialog = new RequestingDialog(SplashScreen.frame);
                    dialog.display();

                    new SwingWorker<Boolean, Void>() {
                        @Override
                        protected Boolean doInBackground() throws Exception {
                            boolean accepted;
                            accepted = chatSender.connect(contact);
                            return accepted;
                        }

                        @Override
                        protected void done() {
                            try {
                                boolean accepted = get();
                                dialog.close();

                                if (accepted) {
                                    // Stop the broadcast and exchange thread
                                    broadcastThread.interrupt();
                                    broadcastListenerThread.interrupt();
                                    exchangeListenerThread.interrupt();
                                    chatListenerThread.interrupt();

                                    SplashScreen.changePanel(ChatGUI.getChatPanel());
                                } else {
                                    new RefusedDialog(SplashScreen.frame).display();
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

        JScrollPane contactScrollPane = new JScrollPane(contactList);
        contactScrollPane.setBorder(null);
        contactListPanel.add(contactScrollPane);
    }

    private void initContactManager() throws IOException {
        String database = DataBase.getDataBasePath(user.getId());

        try {
            ContactDataBase.initialization(database);
            contactManager = new ContactManager();
            contactManager.addContact(ContactDataBase.getContactFromDatabase(user, database));
        } catch (SQLException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                 IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException |
                 InvalidKeySpecException ex) {
            Error dialog = new Error(SplashScreen.frame, ex);
            dialog.display();
        }
    }

    public static JPanel getContact() {
        return new ContactGUI().contactPanel;
    }

    private void createUIComponents() {
        ImageIcon logoIcon = new ImageIcon(Objects.requireNonNull(ContactGUI.class.getResource("/CyChatReversed.png")));
        topBarLogo = new JLabel(logoIcon);

        contactListPanel = new JPanel(new BorderLayout());
        peerListPanel = new JPanel(new BorderLayout());
    }
}
