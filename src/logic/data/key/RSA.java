package logic.data.key;

import logic.security.Encoder;
import logic.security.Generator;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * RSA wrapper class to store RSA key in the form of KeyPair and its encoded form.
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class RSA {
    private final KeyPair keyRSA;
    private final String encodedPublicKey, encodedPrivateKey;

    public RSA() throws NoSuchAlgorithmException {
        this.keyRSA = Generator.generateRSAKey();
        this.encodedPublicKey = Encoder.encode(this.keyRSA.getPublic().getEncoded());
        this.encodedPrivateKey = Encoder.encode(this.keyRSA.getPrivate().getEncoded());
    }

    public RSA(String encodedPublicKey, String encodedPrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PublicKey publicKey = Encoder.decodeRSAPublicKey(encodedPublicKey);
        PrivateKey privateKey = Encoder.decodeRSAPrivateKey(encodedPrivateKey);
        this.keyRSA = new KeyPair(publicKey, privateKey);
        this.encodedPublicKey = encodedPublicKey;
        this.encodedPrivateKey = encodedPrivateKey;
    }

    public PublicKey getPublicKey() {
        return keyRSA.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keyRSA.getPrivate();
    }

    public String getEncodedPublicKey() {
        return encodedPublicKey;
    }

    public String getEncodedPrivateKey() {
        return encodedPrivateKey;
    }
}
