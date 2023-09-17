package logic.manager;

import logic.data.Peer;

import java.util.ArrayList;

public class PeerManager {
    ArrayList<Peer> peers;

    public PeerManager() {
        peers = new ArrayList<>();
    }

    public boolean checkPeerExist(String id) {
        for (Peer peer: peers) {
            if (peer.id().equals(id)) return true;
        }
        return false;
    }

    public void addPeer(Peer peer) {
        peers.add(peer);
    }

    public Peer getPeer(String id   ) {
        for (Peer peer: peers) {
            if (peer.id().equals(id)) return peer;
        }
        return null;
    }

    public ArrayList<Peer> getPeers() {
        return peers;
    }
}
