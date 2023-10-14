package logic.data.key;

import logic.security.Encoder;
import logic.security.Generator;

import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * MainKey wrapper class to store main key and salt in encoded form for storing and regeneration.
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class MainKey {
    private final SecretKey mainKey;
    private final String encodedSalt;

    public MainKey(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = Generator.generateSalt();
        this.mainKey = Generator.generateMainKey(password, salt);
        this.encodedSalt = Encoder.encode(salt);
    }

    public MainKey(String password, String encodedSalt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = Encoder.decode(encodedSalt);
        this.mainKey = Generator.generateMainKey(password, salt);
        this.encodedSalt = encodedSalt;
    }

    public SecretKey getMainKey() {
        return mainKey;
    }

    public String getEncodedSalt() {
        return encodedSalt;
    }
}
