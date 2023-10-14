package logic.data.user;

/**
 * Peer class to store peer's information.
 *
 * @param userId    The user id of the peer.
 * @param username  The username of the peer.
 * @param ipAddress The ip address of the peer.
 * @author Reishandy (isthisruxury@gmail.com)
 */
public record Peer(String userId, String username, String ipAddress) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Peer peer)) return false;

        if (!userId.equals(peer.userId)) return false;
        if (!username.equals(peer.username)) return false;
        return ipAddress.equals(peer.ipAddress);
    }
}
