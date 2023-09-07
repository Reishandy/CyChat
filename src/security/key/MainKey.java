package security.key;

import security.Crypto;
import security.KeyString;

import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class MainKey {
    private final SecretKey key;
    private final byte[] salt;
    private final String saltString;

    public MainKey(String hashedPassword) throws NoSuchAlgorithmException, InvalidKeySpecException {
        salt = Crypto.generateSalt();
        key = Crypto.generateMainKey(hashedPassword, salt);
        saltString = KeyString.SaltToString(salt);
    }

    public MainKey(String hashedPassword, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        saltString = salt;
        this.salt = KeyString.StringToSalt(saltString);
        key = Crypto.generateMainKey(hashedPassword, this.salt);
    }

    public SecretKey getKey() {
        return key;
    }

    public String getSaltString() {
        return saltString;
    }
}
