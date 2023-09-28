package logic.network;

import logic.data.Contact;
import logic.data.Peer;
import logic.data.User;
import logic.manager.ContactManager;
import logic.security.KeyString;
import logic.storage.ContactDataBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ExchangeTest {
    String senderUserName, senderPassword, receiverUserName, receiverPassword, testIpAddress, database;
    User senderUser, receiverUser;
    Peer receiverPeer;
    ContactManager senderContactManager, receiverContactManager;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException, InvalidKeySpecException, SocketException, SQLException {
        testIpAddress = Address.getLocalIp();

        senderUserName = "Cat";
        senderPassword = "I like cats";
        senderUser = new User(senderUserName, senderPassword);
        senderContactManager = new ContactManager();

        receiverUserName = "Dog";
        receiverPassword = "I hate cats";
        receiverUser = new User(receiverUserName, receiverPassword);
        receiverContactManager = new ContactManager();
        receiverPeer = new Peer(receiverUser.getId(), receiverUserName, testIpAddress);

        database = "jdbc:sqlite:./db/" + senderUser.getId() + ".db";
        ContactDataBase.initialization(database);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(Paths.get("./db/"))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .forEach(File::delete);
    }

    void refused() throws InvalidAlgorithmParameterException, SQLException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, InvalidKeyException, InterruptedException {
        boolean status = Exchange.knowEachOther(senderUser, receiverPeer, senderContactManager, database);

        assertFalse(status);
        assertEquals(1, senderContactManager.getContacts().size());
        assertEquals(1, receiverContactManager.getContacts().size());
    }

    @Test
    void handshakeTest() throws NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException, IOException, SQLException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Runnable listenerTask = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    receiverContactManager = Exchange.listener(receiverUser, receiverContactManager,  database, new JFrame());
                } catch (IOException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                         BadPaddingException | InvalidKeyException | InterruptedException | InvalidKeySpecException |
                         InvalidAlgorithmParameterException | SQLException e) {
                    fail("Mo error should be thrown");
                }
            }
        };
        Thread listenerThread = new Thread(listenerTask);
        listenerThread.start();

        boolean status = Exchange.knowEachOther(senderUser, receiverPeer, senderContactManager, database);

        Thread.sleep(1000);

        assertTrue(status);
        assertEquals(1, senderContactManager.getContacts().size());
        assertEquals(1, receiverContactManager.getContacts().size());
        assertTrue(senderContactManager.checkContactExist(receiverUser.getId()));
        assertTrue(receiverContactManager.checkContactExist(senderUser.getId()));

        Contact senderGetFromReceiver = receiverContactManager.getContact(senderUser.getId());
        Contact receiverGetFromSender = senderContactManager.getContact(receiverUser.getId());

        assertEquals(senderUser.getId(), senderGetFromReceiver.getId());
        assertEquals(senderUserName, senderGetFromReceiver.getUserName());
        assertEquals(KeyString.PublicKeyToString(senderUser.getPublicKey()), senderGetFromReceiver.getPublicKeyString());

        assertEquals(receiverUser.getId(), receiverGetFromSender.getId());
        assertEquals(receiverUserName, receiverGetFromSender.getUserName());
        assertEquals(KeyString.PublicKeyToString(receiverUser.getPublicKey()), receiverGetFromSender.getPublicKeyString());

        refused();

        listenerThread.interrupt();
    }
}