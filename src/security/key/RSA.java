package security.key;

import security.Crypto;
import security.KeyString;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class RSA {
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private String publicKeyString, privateKeyString;

    public void initialization() throws NoSuchAlgorithmException {
        KeyPair keyPair = Crypto.generateRSAKey();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
        publicKeyString = KeyString.PublicKeyToString(publicKey);
        privateKeyString = KeyString.PrivateKeyToString(privateKey);
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
        privateKeyString = KeyString.PublicKeyToString(publicKey);
    }

    public void setPublicKey(String publicKeyString) {
        this.privateKeyString = publicKeyString;
        publicKey = KeyString.StringToPublicKey(publicKeyString);
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
