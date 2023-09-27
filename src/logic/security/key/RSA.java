package logic.security.key;

import logic.security.Crypto;
import logic.security.KeyString;

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

    public RSA(String publicKeyString) {
        this.publicKeyString = publicKeyString;
        this.publicKey = KeyString.StringToPublicKey(publicKeyString);
        this.privateKey = null;
        this.privateKeyString = null;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public String getPublicKeyString() {
        return publicKeyString;
    }

    public String getPrivateKeyString() {
        return privateKeyString;
    }
}
