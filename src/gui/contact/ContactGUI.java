package gui.contact;

import gui.bootup.SplashScreen;
import logic.data.Contact;
import logic.data.Peer;
import logic.data.User;
import logic.manager.ContactManager;
import logic.manager.PeerManager;
import logic.network.Broadcast;
import logic.network.ChatReceiver;
import logic.network.ChatSender;
import logic.network.Exchange;
import logic.storage.ContactDataBase;
import logic.storage.DataBase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Vector;

public class ContactGUI {
    public static ContactManager contactManager;
    public static PeerManager peerManager;
    public static ChatSender chatSender;
    public static ChatReceiver chatReceiver;

    private final User user;
    private Thread broadcastThread;
    private Thread broadcastListenerThread;
    private Thread exchangeListenerThread;


    private JPanel contactPanel;
    private JPanel topBarPanel;
    private JPanel contactListPanel;
    private JPanel peerListPanel;
    private JLabel topBarLogo;
    private JLabel contactTitle;
    private JLabel peerTitle;

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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Broadcast own detail
        broadcastThread = new Thread(() -> {
            try {
                Broadcast.broadcast(user.getId(), user.getUserName());
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        });
        broadcastThread.start();

        // Listen for other broadcast // TODO: Test this
        broadcastListenerThread = new Thread(() -> {
           Broadcast.listenForBroadcast(user.getId(), contactManager, peerManager);
        });
        broadcastListenerThread.start();

        // Listen for contact exchange request // TODO: test this
        exchangeListenerThread = new Thread(() -> {
            Exchange.listener(user, contactManager);
        });
        exchangeListenerThread.start();

        // Chat boot-up // TODO: test this
        try {
            chatSender = new ChatSender(user, DataBase.getDataBasePath(user.getId()));
            chatReceiver = new ChatReceiver(user, contactManager, DataBase.getDataBasePath(user.getId()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // TODO: chatReceiver method... that will go to the chatGUI
        //      make a method on the chat receiver to check if connected
        //      using while true loop and on separate thread

        // TODO: disable and re-enable broadcast and exchange thread

        // TODO: make this regally update, using thread?
        // Display contact and peer
        displayContacts();
        displayPeers();
    }

    private void displayPeers() {
        JList<Peer> peerList = new JList<>(new Vector<>(peerManager.getPeers()));
        peerList.setCellRenderer(new PeerCellRenderer());

        peerList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // Stop the broadcast and exchange thread
                    broadcastThread.interrupt();
                    broadcastListenerThread.interrupt();
                    exchangeListenerThread.interrupt();

                    // TODO: exchange contact
                    int index = peerList.locationToIndex(e.getPoint());
                    Peer peer = peerList.getModel().getElementAt(index);
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
                    // Stop the broadcast and exchange thread
                    broadcastThread.interrupt();
                    broadcastListenerThread.interrupt();
                    exchangeListenerThread.interrupt();

                    // TODO: connect
                    int index = contactList.locationToIndex(e.getPoint());
                    Contact contact = contactList.getModel().getElementAt(index);
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
