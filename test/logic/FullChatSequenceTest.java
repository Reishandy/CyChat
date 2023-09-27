package logic;

import logic.data.Constant;
import logic.data.Contact;
import logic.data.User;
import logic.manager.ContactManager;
import logic.network.Address;
import logic.network.ChatReceiver;
import logic.network.ChatSender;
import logic.security.Crypto;
import logic.security.KeyString;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class FullChatSequenceTest {
    String senderName, senderPassword, receiverName, receiverPassword;
    String database, message1, message2;
    String aesKeyString, ivString;
    User sender, receiver;
    Contact senderContact, receiverContact;
    ContactManager receiverContactManager;
    ChatSender chatSender;
    ChatReceiver chatReceiver;

    @AfterEach
    void clear() throws SQLException {
        Connection connection = DriverManager.getConnection(database);

        PreparedStatement statement1 = connection.prepareStatement("DROP TABLE IF EXISTS " + sender.getId());
        statement1.executeUpdate();
        PreparedStatement statement2 = connection.prepareStatement("DROP TABLE IF EXISTS " + receiver.getId());
        statement2.executeUpdate();

        statement1.close();
        statement2.close();
        connection.close();
    }

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException, InvalidKeySpecException, UnknownHostException, SocketException {
        database = "jdbc:sqlite:test.db";
        senderName = "Cat";
        receiverName = "Dog";
        senderPassword = "I like cats";
        receiverPassword = "I hate cats";

        message1 = "Water is better than Fire!";
        message2 = "No!! Fire is way better that Water";

        aesKeyString = KeyString.SecretKeyToString(Crypto.generateAESKey(Constant.KEY_SIZE_AES_128));
        ivString = KeyString.IvToString(Crypto.generateIv());

        sender = new User(senderName, senderPassword);
        receiver = new User(receiverName, receiverPassword);
        senderContact = new Contact(sender.getId(), senderName, KeyString.PublicKeyToString(sender.getPublicKey()), aesKeyString, ivString);
        receiverContact = new Contact(receiver.getId(), receiverName, KeyString.PublicKeyToString(receiver.getPublicKey()), aesKeyString, ivString);
        senderContact.setIp(Address.getLocalIp());
        receiverContact.setIp(Address.getLocalIp());


        receiverContactManager = new ContactManager();
        receiverContactManager.addContact(senderContact);

        chatSender = new ChatSender(sender, database);
        chatReceiver = new ChatReceiver(receiver, receiverContactManager, database, new JFrame());
    }

    @Test
    void senderTest() throws InterruptedException {
        ArrayList<String> receivedMessageSender = new ArrayList<>();

        Thread receiverTestThread = new Thread(() -> {
            try {
                receiverTest();
            } catch (InterruptedException e) {
                fail("InterruptedException should not be throws");
            }
        });

        try {
            boolean connected = chatSender.connect(receiverContact);
            assertTrue(connected);
            receiverTestThread.start();

            Runnable receiveMessageTask = () -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        String receivedMessage = chatSender.receive();
                        if (receivedMessage == null) continue;

                        receivedMessageSender.add(receivedMessage);
                    } catch (SocketException e) {
                        return;
                    } catch (IOException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                             IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
                             InvalidKeyException e) {
                        fail("No errors should be thrown");
                    }}};
            Thread revieveMessageThread = new Thread(receiveMessageTask);
            revieveMessageThread.start();

            Thread.sleep(1000);

            chatSender.send(message1);

            Thread.sleep(500);
            assertEquals(1, receivedMessageSender.size());
            assertEquals(message2, receivedMessageSender.get(0));

            revieveMessageThread.interrupt();
            chatSender.closeSession();

        } catch (IOException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            fail("No errors should be thrown");
        }

        receiverTestThread.join();
    }

    void receiverTest() throws InterruptedException {
        ArrayList<String> receivedMessageReceiver = new ArrayList<>();

        try {
            Runnable receiveMessageTask = () -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        String receivedMessage = chatReceiver.receive();
                        if (receivedMessage == null) continue;

                        receivedMessageReceiver.add(receivedMessage);
                    } catch (SocketException e) {
                        return;
                    } catch (IOException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                             IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
                             InvalidKeyException e) {
                        fail("No errors should be thrown");
                    }}};
            Thread revieveMessageThread = new Thread(receiveMessageTask);
            revieveMessageThread.start();

            Thread.sleep(1000);

            chatReceiver.send(message2);

            Thread.sleep(500);
            assertEquals(1, receivedMessageReceiver.size());
            assertEquals(message1, receivedMessageReceiver.get(0));

            revieveMessageThread.interrupt();
            chatReceiver.closeSession();
            chatReceiver.closeListener();

        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | InterruptedException |
                 IOException e) {
            fail("No errors should be thrown");
        }
    }
}