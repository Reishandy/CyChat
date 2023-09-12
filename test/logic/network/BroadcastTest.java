package logic.network;

import logic.data.Constant;
import logic.data.Contact;
import logic.data.Peer;
import logic.manager.ContactManager;
import logic.manager.PeerManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import logic.security.Crypto;
import logic.security.KeyString;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class BroadcastTest {
    ContactManager contactManager;
    PeerManager peerManager;
    String userName, localHostIpAddress, testIpAddress, newUserName;
    Contact contact;

    @BeforeEach
    void setUp() {
        try {
            contactManager = new ContactManager();
            peerManager = new PeerManager();

            userName = "Cat";
            localHostIpAddress = Inet4Address.getLocalHost().getHostAddress();
            testIpAddress = incrementIPAddress(localHostIpAddress, 10);

            contact = new Contact("Dog", KeyString.PublicKeyToString(Crypto.generateRSAKey().getPublic()),
                    KeyString.SecretKeyToString(Crypto.generateAESKey(Constant.keySizeAES128)),
                    KeyString.IvToString(Crypto.generateIv()));
            contactManager.addContact(contact);

            newUserName = "Dog_haters_123_xXx";
        } catch (NoSuchAlgorithmException | UnknownHostException e) {
            fail("No errors should be thrown");
        }
    }

    @Test
    void broadcastTest() throws InterruptedException {
        Runnable broadcastTask = () -> {
            try {
                Broadcast.broadcast(userName);
            } catch (UnknownHostException e) {
                fail("UnknownHostException should not be thrown");
            }
        };
        Thread broadcastThread = new Thread(broadcastTask);
        broadcastThread.start();
        Thread.sleep(5005);

        String[] receivedMessage = listenTest();
        assertNotNull(receivedMessage);

        String getUserName = receivedMessage[0];
        String getIpAddress = receivedMessage[1];
        assertNotNull(getUserName);
        assertNotNull(getIpAddress);

        assertEquals(userName, getUserName);
        assertEquals(localHostIpAddress, getIpAddress);

        broadcastThread.interrupt();
    }

    @Test
    void listenForBroadcastTest() throws InterruptedException {
        Runnable listenForBroadcastTask = () -> {
            Broadcast.listenForBroadcast(userName, contactManager, peerManager);
        };
        Thread listenThread = new Thread(listenForBroadcastTask);
        listenThread.start();

        // Test for own broadcast
        broadcastForListenTest(userName, localHostIpAddress);
        Thread.sleep(100); // Wait first so that the listener can capture and do some magic
        assertEquals(1, contactManager.getContacts().size());
        assertTrue(peerManager.getPeers().isEmpty());

        // Test for contact
        broadcastForListenTest(contact.getUserName(), testIpAddress);
        Thread.sleep(100);
        assertTrue(contactManager.checkContactExist("Dog"));

        Contact getContact = contactManager.getContact("Dog");
        assertNotNull(getContact);
        assertEquals(testIpAddress, getContact.getIp());

        // Test for peer
        broadcastForListenTest(newUserName, testIpAddress);
        Thread.sleep(100);
        assertEquals(1, peerManager.getPeers().size());
        assertTrue(peerManager.checkPeerExist(newUserName));

        Peer getPeer = peerManager.getPeer(newUserName);
        assertNotNull(getPeer);
        assertEquals(testIpAddress, getPeer.ip());

        listenThread.interrupt();
    }

    String[] listenTest() {
        try (DatagramSocket listenSocket = new DatagramSocket(Constant.broadcastPort)) {
            byte[] buffer = new byte[Constant.bufferListenForBroadcast];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            listenSocket.receive(packet);

            return new String(packet.getData(), 0, packet.getLength()).split(":");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void broadcastForListenTest(String userName, String ipAddress) {
        try (DatagramSocket broadcastSocket = new DatagramSocket();) {
            String broadcastMessage = userName + ":" + ipAddress;

            DatagramPacket packet = new DatagramPacket(
                    broadcastMessage.getBytes(),
                    broadcastMessage.length(),
                    Inet4Address.getByName("255.255.255.255"),
                    Constant.broadcastPort
            );
            broadcastSocket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Ugly code straight from gpt
    // Eh..... i dont care...
    public static String incrementIPAddress(String ipAddress, int increment) {
        String[] octets = ipAddress.split("\\.");
        int lastOctet = Integer.parseInt(octets[3]);
        lastOctet = (lastOctet + increment) % 256;
        return octets[0] + "." + octets[1] + "." + octets[2] + "." + lastOctet;
    }
}