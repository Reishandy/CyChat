package logic.data;

import java.util.Objects;

public record Peer(String id, String userName, String ip) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Peer peer)) return false;

        if (!Objects.equals(userName, peer.userName)) return false;
        return Objects.equals(ip, peer.ip);
    }

}
