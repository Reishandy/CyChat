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
    public void testAddPeer() {
        Peer peer = new Peer("PeerA", "192.168.2.1");
        peerManager.addPeer(peer);
        assertTrue(peerManager.findPeer(peer));
    }

    @Test
    public void testFindPeer() {
        Peer peer = new Peer("PeerB", "192.168.2.2");
        assertFalse(peerManager.findPeer(peer));
        peerManager.addPeer(peer);
        assertTrue(peerManager.findPeer(peer));
    }

    @Test
    public void testGetPeers() {
        Peer peer1 = new Peer("PeerC", "192.168.2.3");
        Peer peer2 = new Peer("PeerD", "192.168.2.4");
        peerManager.addPeer(peer1);
        peerManager.addPeer(peer2);
        assertEquals(2, peerManager.getPeers().size());
    }
}