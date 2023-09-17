package logic.data;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PeerTest {
    @Test
    void peerTest() {
        String id = "CyChat_" + UUID.randomUUID().toString().replaceAll("-", "_");
        String userName = "Cat";
        String ip = "192.168.1.1";
        Peer peer = new Peer(id, userName, ip);

        assertNotNull(peer.id());
        assertNotNull(peer.ip());
        assertNotNull(peer.userName());
        assertNotNull(peer.userName(), userName);
        assertNotNull(peer.ip(), ip);
    }
}