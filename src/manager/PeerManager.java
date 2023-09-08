package manager;

import data.Peer;

import java.util.ArrayList;

public class PeerManager {
    ArrayList<Peer> peers;

    public PeerManager() {
        peers = new ArrayList<>();
    }

    public boolean findPeer(Peer peer) {
        return peers.contains(peer);
    }

    public void addPeer(Peer peer) {
        peers.add(peer);
    }

    public ArrayList<Peer> getPeers() {
        return peers;
    }
}
