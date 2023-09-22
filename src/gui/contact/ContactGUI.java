package gui.contact;

import gui.bootup.SplashScreen;
import logic.data.Contact;
import logic.data.Peer;
import logic.manager.ContactManager;
import logic.manager.PeerManager;
import logic.security.Crypto;
import logic.security.KeyString;
import logic.storage.ContactDataBase;
import logic.storage.DataBase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Random;
import java.util.Vector;

public class ContactGUI {
    public static ContactManager contactManager;
    public static PeerManager peerManager;

    private JPanel contactPanel;
    private JPanel topBarPanel;
    private JPanel contactListPanel;
    private JPanel peerListPanel;
    private JLabel topBarLogo;
    private JLabel contactTitle;
    private JLabel peerTitle;

    public ContactGUI() {
        topBarPanel.setBackground(new Color(140, 82, 255));
        contactListPanel.setBackground(Color.WHITE);
        peerListPanel.setBackground(Color.WHITE);

        // TODO: add some loading animation

        try {
            initContactManager();
            peerManager = new PeerManager();

            // TODO: DEBUG simulate getting contact and discovering peer
            for (int i = 0; i < 30; i++) {
                contactManager.addContact(new Contact(
                        String.valueOf(new Random().nextInt(100)),
                        String.valueOf(new Random().nextInt(100)),
                        KeyString.PublicKeyToString(Crypto.generateRSAKey().getPublic()),
                        KeyString.SecretKeyToString(Crypto.generateAESKey(128)),
                        KeyString.IvToString(Crypto.generateIv())
                ));

                peerManager.addPeer(new Peer(
                        String.valueOf(new Random().nextInt(1000)),
                        String.valueOf(new Random().nextInt(1000)),
                        String.valueOf(new Random().nextInt(1000))
                ));
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // TODO: chat mechanism boot-up

        // TODO: make this regally update
        displayContacts();
        displayPeers();

        // TODO: chatReceiver method... that will go to the chatGUI
    }

    private void displayPeers() {
        JList<Peer> peerList = new JList<>(new Vector<>(peerManager.getPeers()));
        peerList.setCellRenderer(new PeerCellRenderer());

        peerList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = peerList.locationToIndex(e.getPoint());
                    // TODO: exchange contact
                    Peer peer = peerList.getModel().getElementAt(index);

                    // TODO: DEBUG
                    peerTitle.setText(String.valueOf(new Random().nextInt(100)));
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
                    // TODO: connect
                    Contact contact = contactList.getModel().getElementAt(index);

                    // TODO: DEBUG
                    peerTitle.setText(String.valueOf(new Random().nextInt(100)));
                }
            }
        });

        JScrollPane contactScrollPane = new JScrollPane(contactList);
        contactScrollPane.setBorder(null);
        contactListPanel.add(contactScrollPane);
    }

    private void initContactManager() throws IOException {
        String database = DataBase.getDataBasePath(SplashScreen.user.getId());

        ContactDataBase.initialization(database);
        contactManager = new ContactManager();
        contactManager.addContact(ContactDataBase.getContactFromDatabase(SplashScreen.user, database));
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
