package logic.data.user;

/**
 * History class to store chat information, from user id, username, date time, encrypted message, and iv for the message.
 *
 * @param userId           The user id of the peer.
 * @param username         The username of the peer.
 * @param dateTime         The date time of the message.
 * @param encryptedMessage The encrypted message.
 * @param encodedIv        The iv for the message.
 * @author Reishandy (isthisruxury@gmail.com)
 */
public record History(String userId, String username, String dateTime, String encryptedMessage, String encodedIv) {
}
