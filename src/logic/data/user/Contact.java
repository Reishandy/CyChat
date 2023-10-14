package logic.data.user;

import logic.data.key.AES;

import javax.crypto.SecretKey;

/**
 * Contact class to store contact information, from user id, username, ipAddress, and AES chat key.
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class Contact {
    private final String userId, username;
    private String ipAddress;
    private final AES keyChatAES;

    public Contact(String userId, String username, String encodedKeyChatAES) {
        this.userId = userId;
        this.username = username;
        this.ipAddress = "NONE";
        this.keyChatAES = new AES(encodedKeyChatAES);
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public SecretKey getKeyChatAES() {
        return keyChatAES.getKey();
    }

    public String getEncodedKeyChatAES() {
        return keyChatAES.getEncodedKey();
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
