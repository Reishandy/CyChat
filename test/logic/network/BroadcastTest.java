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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BroadcastTest {
    ContactManager contactManager;
    PeerManager peerManager;
    String id, userName, localHostIpAddress, testIpAddress, newUserName, newId, dogId;
    Contact contact;

    @BeforeEach
    void setUp() {
        try {
            contactManager = new ContactManager();
            peerManager = new PeerManager();

            id = "CyChat_" + UUID.randomUUID().toString().replaceAll("-", "_");
            dogId = "CyChat_" + UUID.randomUUID().toString().replaceAll("-", "_");
            userName = "Cat";
            localHostIpAddress = Inet4Address.getLocalHost().getHostAddress();
            testIpAddress = incrementIPAddress(localHostIpAddress, 10);

            contact = new Contact(dogId, "Dog", KeyString.PublicKeyToString(Crypto.generateRSAKey().getPublic()),
                    KeyString.SecretKeyToString(Crypto.generateAESKey(Constant.keySizeAES128)),
                    KeyString.IvToString(Crypto.generateIv()));
            contactManager.addContact(contact);

            newId = "CyChat_" + UUID.randomUUID().toString().replaceAll("-", "_");
            newUserName = "Dog_haters_123_xXx";
        } catch (NoSuchAlgorithmException | UnknownHostException e) {
            fail("No errors should be thrown");
        }
    }

    @Test
    void broadcastTest() throws InterruptedException {
        Runnable broadcastTask = () -> {
            try {
                Broadcast.broadcast(id, userName);
            } catch (UnknownHostException e) {
                fail("UnknownHostException should not be thrown");
            }
        };
        Thread broadcastThread = new Thread(broadcastTask);
        broadcastThread.start();
        Thread.sleep(5005);

        String[] receivedMessage = listenTest();
        assertNotNull(receivedMessage);

        String getId = receivedMessage[0];
        String getUserName = receivedMessage[1];
        String getIpAddress = receivedMessage[2];
        assertNotNull(getId);
        assertNotNull(getUserName);
        assertNotNull(getIpAddress);

        assertEquals(id, getId);
        assertEquals(userName, getUserName);
        assertEquals(localHostIpAddress, getIpAddress);

        broadcastThread.interrupt();
    }

    @Test
    void listenForBroadcastTest() throws InterruptedException {
        Runnable listenForBroadcastTask = () -> {
            Broadcast.listenForBroadcast(id, contactManager, peerManager);
        };
        Thread listenThread = new Thread(listenForBroadcastTask);
        listenThread.start();

        // Test for own broadcast
        broadcastForListenTest(id, userName, localHostIpAddress);
        Thread.sleep(100); // Wait first so that the listener can capture and do some magic
        assertEquals(1, contactManager.getContacts().size());
        assertTrue(peerManager.getPeers().isEmpty());

        // Test for contact
        broadcastForListenTest(contact.getId(), contact.getUserName(), testIpAddress);
        Thread.sleep(100);
        assertTrue(contactManager.checkContactExist(dogId));

        Contact getContact = contactManager.getContact(dogId);
        assertNotNull(getContact);
        assertEquals(testIpAddress, getContact.getIp());

        // Test for peer
        broadcastForListenTest(newId, newUserName, testIpAddress);
        Thread.sleep(100);
        assertEquals(1, peerManager.getPeers().size());
        assertTrue(peerManager.checkPeerExist(newId));

        Peer getPeer = peerManager.getPeer(newId);
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

    void broadcastForListenTest(String id, String userName, String ipAddress) {
        try (DatagramSocket broadcastSocket = new DatagramSocket();) {
            String broadcastMessage = id + ":" + userName + ":" + ipAddress;

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