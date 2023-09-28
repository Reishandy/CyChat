package logic.network;

import logic.data.Constant;
import logic.data.Contact;
import logic.data.ManagersWrapper;
import logic.data.Peer;
import logic.manager.ContactManager;
import logic.manager.PeerManager;
import logic.security.Crypto;
import logic.security.KeyString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
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
            localHostIpAddress = Address.getLocalIp();
            assert localHostIpAddress != null;
            testIpAddress = incrementIPAddress(localHostIpAddress, 10);

            contact = new Contact(dogId, "Dog", KeyString.PublicKeyToString(Crypto.generateRSAKey().getPublic()),
                    KeyString.SecretKeyToString(Crypto.generateAESKey(Constant.KEY_SIZE_AES_128)),
                    KeyString.IvToString(Crypto.generateIv()));
            contactManager.addContact(contact);

            newId = "CyChat_" + UUID.randomUUID().toString().replaceAll("-", "_");
            newUserName = "Dog_haters_123_xXx";
        } catch (NoSuchAlgorithmException | SocketException | InvalidKeySpecException e) {
            fail("No errors should be thrown");
        }
    }

    @Test
    void broadcastTest() throws InterruptedException {
        Runnable broadcastTask = () -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Broadcast.broadcast(id, userName);
                }
            } catch (IOException e) {
                fail("UnknownHostException should not be thrown");
            }
        };
        Thread broadcastThread = new Thread(broadcastTask);
        broadcastThread.start();

        Thread.sleep(3000);

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
            while (!Thread.currentThread().isInterrupted()) {
                ManagersWrapper managersWrapper = null;
                try {
                    managersWrapper = Broadcast.listenForBroadcast(id, contactManager, peerManager);
                } catch (IOException e) {
                    fail("Mo error should be thrown");
                }
                if (managersWrapper == null) continue;
                contactManager = managersWrapper.contactManager();
                peerManager = managersWrapper.peerManager();
            }
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
        try (DatagramSocket listenSocket = new DatagramSocket(Constant.BROADCAST_PORT)) {
            byte[] buffer = new byte[Constant.BUFFER_LISTEN_FOR_BROADCAST];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            listenSocket.receive(packet);

            return new String(packet.getData(), 0, packet.getLength()).split(":");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void broadcastForListenTest(String id, String userName, String ipAddress) {
        try (DatagramSocket broadcastSocket = new DatagramSocket()) {
            String broadcastMessage = id + ":" + userName + ":" + ipAddress;

            DatagramPacket packet = new DatagramPacket(
                    broadcastMessage.getBytes(),
                    broadcastMessage.length(),
                    Inet4Address.getByName(Address.getBroadcastAddress()),
                    Constant.BROADCAST_PORT
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