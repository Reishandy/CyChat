package security.key;

import security.Crypto;
import security.KeyString;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class RSA {
    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    private final String publicKeyString, privateKeyString;

    public RSA() throws NoSuchAlgorithmException {
        KeyPair keyPair = Crypto.generateRSAKey();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
        publicKeyString = KeyString.PublicKeyToString(publicKey);
        privateKeyString = KeyString.PrivateKeyToString(privateKey);
    }

    public RSA(String publicKeyString, String privateKeyString) {
        this.publicKeyString = publicKeyString;
        this.privateKeyString = privateKeyString;
        this.privateKey = KeyString.StringToPrivateKey(privateKeyString);
        this.publicKey = KeyString.StringToPublicKey(publicKeyString);
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        if (privateKey == null) return null;
        return privateKey;
    }

    public String getPublicKeyString() {
        return publicKeyString;
    }

    public String getPrivateKeyString() {
        if (privateKeyString == null) return null;
        return privateKeyString;
    }
}
