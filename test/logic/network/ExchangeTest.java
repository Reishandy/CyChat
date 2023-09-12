package logic.network;

import logic.data.Contact;
import logic.data.Peer;
import logic.data.User;
import logic.manager.ContactManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import logic.security.KeyString;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;

class ExchangeTest {
    String senderUserName, senderPassword, receiverUserName, receiverPassword, testIpAddress;
    User senderUser, receiverUser;
    Peer receiverPeer;
    ContactManager senderContactManager, receiverContactManager;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException, InvalidKeySpecException, UnknownHostException {
        testIpAddress = Inet4Address.getLocalHost().getHostAddress();

        senderUserName = "Cat";
        senderPassword = "I like cats";
        senderUser = new User(senderUserName, senderPassword);
        senderContactManager = new ContactManager();

        receiverUserName = "Dog";
        receiverPassword = "I hate cats";
        receiverUser = new User(receiverUserName, receiverPassword);
        receiverContactManager = new ContactManager();
        receiverPeer = new Peer(receiverUserName, testIpAddress);

        // TODO: automate decision
    }

    void refused() {
        boolean status = Exchange.knowEachOther(senderUser, receiverPeer, senderContactManager);

        assertFalse(status);
        assertEquals(0, senderContactManager.getContacts().size());
        assertEquals(0, receiverContactManager.getContacts().size());
    }

    @Test
    void handshakeTest() throws UnknownHostException, NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException {
        Runnable listenerTask = () -> {
            Exchange.listener(receiverUser, receiverContactManager);
        };
        Thread listenerThread = new Thread(listenerTask);
        listenerThread.start();

        boolean status = Exchange.knowEachOther(senderUser, receiverPeer, senderContactManager);

        Thread.sleep(100);

        assertTrue(status);
        assertEquals(1, senderContactManager.getContacts().size());
        assertEquals(1, receiverContactManager.getContacts().size());
        assertTrue(senderContactManager.checkContactExist(receiverUserName));
        assertTrue(receiverContactManager.checkContactExist(senderUserName));

        Contact senderGetFromReceiver = receiverContactManager.getContact(senderUserName);
        Contact receiverGetFromSender = senderContactManager.getContact(receiverUserName);

        assertEquals(senderUserName, senderGetFromReceiver.getUserName());
        assertEquals(KeyString.PublicKeyToString(senderUser.getPublicKey()), senderGetFromReceiver.getPublicKeyString());

        assertEquals(receiverUserName, receiverGetFromSender.getUserName());
        assertEquals(KeyString.PublicKeyToString(receiverUser.getPublicKey()), receiverGetFromSender.getPublicKeyString());

        setUp();
        refused();

        listenerThread.interrupt();
    }
}