package gui.contact;

import gui.bootup.SplashScreen;
import gui.chat.ChatGUI;
import gui.dialog.RefusedDialog;
import gui.dialog.RequestingDialog;
import logic.data.Constant;
import logic.data.Contact;
import logic.data.Peer;
import logic.data.User;
import logic.manager.ContactManager;
import logic.manager.ManagersWrapper;
import logic.manager.PeerManager;
import logic.network.Broadcast;
import logic.network.ChatReceiver;
import logic.network.ChatSender;
import logic.network.Exchange;
import logic.security.Crypto;
import logic.security.KeyString;
import logic.storage.ContactDataBase;
import logic.storage.DataBase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

public class ContactGUI {
    public static ContactManager contactManager;
    public static PeerManager peerManager;
    public static ChatSender chatSender;
    public static ChatReceiver chatReceiver;
    private final User user;
    private final Thread broadcastThread;
    private final Thread broadcastListenerThread;
    private final Thread exchangeListenerThread;


    private JPanel contactPanel;
    private JPanel topBarPanel;
    private JPanel contactListPanel;
    private JPanel peerListPanel;
    private JLabel topBarLogo;
    private JLabel contactTitle;
    private JLabel peerTitle;
    private JLabel contactInstructionLabel;
    private JLabel peerInstructionLabel;

    public ContactGUI() {
        // Set up some ui stuff
        topBarPanel.setBackground(new Color(140, 82, 255));
        contactListPanel.setBackground(Color.WHITE);
        peerListPanel.setBackground(Color.WHITE);

        // Get user from boot up
        this.user = SplashScreen.user;

        // TODO: add some loading animation

        // Load contact and set up peer
        try {
            initContactManager();
            peerManager = new PeerManager();

            // TODO: debug
            contactManager.addContact(new Contact(
                    "123", "cat", KeyString.PublicKeyToString(Crypto.generateRSAKey().getPublic()),
                    KeyString.SecretKeyToString(Crypto.generateAESKey(128)), KeyString.IvToString(Crypto.generateIv())
            ));
            peerManager.addPeer(new Peer("1234", "dog", "192.168.0.0"));
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // Broadcast own detail // TODO: test this
        broadcastThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Broadcast.broadcast(user.getId(), user.getUserName());
                    Thread.sleep(5000);
                } catch (UnknownHostException | InterruptedException ignored) {}
            }
        });
        broadcastThread.start();

        // Listen for other broadcast // TODO: Test this and fix
        broadcastListenerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("aaa"); // TODO
                ManagersWrapper managersWrapper = Broadcast.listenForBroadcast(user.getId(), contactManager, peerManager);
                if (managersWrapper == null) continue;
                contactManager = managersWrapper.getContactManager();
                peerManager = managersWrapper.getPeerManager();
                System.out.println(peerManager.getPeers()); // TODO
                displayPeers();
                displayContacts();
            }
        });
        broadcastListenerThread.start();

        // Listen for contact exchange request // TODO: test this
        exchangeListenerThread = new Thread(() -> {
            Exchange.listener(user, contactManager, SplashScreen.frame);
        });
        exchangeListenerThread.start();

        // Chat boot-up // TODO: test this
        try {
            chatSender = new ChatSender(user, DataBase.getDataBasePath(user.getId()));
            chatReceiver = new ChatReceiver(user, contactManager, DataBase.getDataBasePath(user.getId()), SplashScreen.frame);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // TODO: chatReceiver method... that will go to the chatGUI
        //      make a method on the chat receiver to check if connected
        //      using while true loop and on separate thread

        // Display initial contacts and peers
        displayContacts();
        displayPeers();
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
                            boolean accepted;
                            try {
                                accepted = Exchange.knowEachOther(user, peer, contactManager, DataBase.getDataBasePath(user.getId()));
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                            return accepted;
                        }

                        @Override
                        protected void done() {
                            try {
                                boolean accepted = get();
                                dialog.close();

                                if (!accepted) {
                                    new RefusedDialog(SplashScreen.frame).display();
                                }
                            } catch (ExecutionException | InterruptedException ex) {
                                throw new RuntimeException(ex);
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

                                    SplashScreen.changePanel(ChatGUI.getChatPanel());
                                } else {
                                    new RefusedDialog(SplashScreen.frame).display();
                                }
                            } catch (ExecutionException | InterruptedException ex) {
                                throw new RuntimeException(ex);
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

        ContactDataBase.initialization(database);
        contactManager = new ContactManager();
        contactManager.addContact(ContactDataBase.getContactFromDatabase(user, database));
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
