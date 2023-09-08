package data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeerTest {
    @Test
    void peerTest() {
        String userName = "Cat";
        String ip = "192.168.1.1";
        Peer peer = new Peer(userName, ip);

        assertNotNull(peer.ip());
        assertNotNull(peer.userName());
        assertNotNull(peer.userName(), userName);
        assertNotNull(peer.ip(), ip);
    }
}