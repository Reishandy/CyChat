package logic.data.key;

import logic.security.Encoder;
import logic.security.Generator;

import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

/**
 * AES wrapper class to store AES key and its encoded form.
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class AES {
    private final SecretKey key;
    private final String encodedKey;

    public AES() throws NoSuchAlgorithmException {
        this.key = Generator.generateAESKey();
        this.encodedKey = Encoder.encode(this.key.getEncoded());
    }

    public AES(String encodedKey) {
        this.key = Encoder.decodeAESKey(encodedKey);
        this.encodedKey = encodedKey;
    }

    public SecretKey getKey() {
        return key;
    }

    public String getEncodedKey() {
        return encodedKey;
    }
}
