package manager;

import data.Peer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeerManagerTest {
    private PeerManager peerManager;

    @BeforeEach
    public void setUp() {
        peerManager = new PeerManager();
    }

    @Test
    public void testCheckPeerExist() {
        Peer peer1 = new Peer("User1", "192.168.0.0");
        peerManager.addPeer(peer1);

        assertTrue(peerManager.checkPeerExist("User1"));
        assertFalse(peerManager.checkPeerExist("User2"));
    }

    @Test
    public void testAddPeer() {
        Peer peer1 = new Peer("User1", "192.168.0.0");
        peerManager.addPeer(peer1);

        assertEquals(1, peerManager.getPeers().size());
        assertTrue(peerManager.checkPeerExist("User1"));
    }

    @Test
    public void testGetPeer() {
        Peer peer1 = new Peer("User1", "192.168.0.0");
        peerManager.addPeer(peer1);

        Peer retrievedPeer = peerManager.getPeer("User1");

        assertNotNull(retrievedPeer);
        assertEquals("User1", retrievedPeer.userName());
        assertEquals("192.168.0.0", retrievedPeer.ip());
    }
}