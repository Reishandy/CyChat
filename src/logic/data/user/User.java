package logic.data.user;

import logic.data.Config;
import logic.data.key.MainKey;
import logic.data.key.RSA;
import logic.security.Encoder;
import logic.security.Generator;
import logic.security.Hash;

import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

/**
 * Class to store and create new user data, can be created from beginning (username and password) or from database
 * (all data encoded).
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class User {
    private final String userId, username, hashedPassword;
    private final RSA keyRSA;
    private final MainKey keyMain;
    private final String encodedPasswordSalt;

    public User(String username, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = Generator.generateSalt();
        this.userId = "CyChat_" + UUID.randomUUID().toString().replaceAll("-", "_");
        this.username = username;
        this.hashedPassword = Hash.hash(password, salt, Config.HASH_MODE_PASSWORD);
        this.keyRSA = new RSA();
        this.keyMain = new MainKey(password);
        this.encodedPasswordSalt = Encoder.encode(salt);
    }

    public User(String userId, String username, String password, String hashedPassword, String encodedPasswordSalt,
                String encodedMainKeySalt, String encodedPublicKey, String encodedPrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.userId = userId;
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.keyRSA = new RSA(encodedPublicKey, encodedPrivateKey);
        this.keyMain = new MainKey(password, encodedMainKeySalt);
        this.encodedPasswordSalt = encodedPasswordSalt;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public PublicKey getPublicKey() {
        return keyRSA.getPublicKey();
    }

    public PrivateKey getPrivateKey() {
        return keyRSA.getPrivateKey();
    }

    public SecretKey getMainKey() {
        return keyMain.getMainKey();
    }

    public String getEncodedPasswordSalt() {
        return encodedPasswordSalt;
    }

    public String getEncodedMainKeySalt() {
        return keyMain.getEncodedSalt();
    }
}
