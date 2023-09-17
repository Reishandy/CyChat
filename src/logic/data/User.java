package logic.data;

import logic.security.Crypto;
import logic.security.Hash;
import logic.security.KeyString;
import logic.security.key.MainKey;
import logic.security.key.RSA;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

public class User {
    private final String id, userName, hashedPassword;
    private final RSA keyRSA;
    private final MainKey keyMainKey;

    public User(String userName, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.id = "CyChat_" + UUID.randomUUID().toString().replaceAll("-", "_");
        this.userName = userName;
        this.hashedPassword = Hash.hashPassword(password, userName);
        this.keyRSA = new RSA();
        this.keyMainKey = new MainKey(password);
        password = null;
    }

    public User(String id, String userName, String password, String hashedPassword, String saltString, String ivString, String encryptedPublicKey, String encryptedPrivateKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        this.id = id;
        this.userName = userName;
        this.hashedPassword = hashedPassword;
        this.keyMainKey = new MainKey(password, saltString, ivString);
        password = null;

        String publicKeyString = Crypto.decryptAES(encryptedPublicKey, getMainKey(), getIv());
        String privateKeyString = Crypto.decryptAES(encryptedPrivateKey, getMainKey(), getIv());
        this.keyRSA = new RSA(publicKeyString, privateKeyString);
    }

    public String getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public PrivateKey getPrivateKey() {
        return keyRSA.getPrivateKey();
    }

    public PublicKey getPublicKey() {
        return keyRSA.getPublicKey();
    }

    public SecretKey getMainKey() {
        return keyMainKey.getKey();
    }

    public String getSaltString() {
        return keyMainKey.getSaltString();
    }

    public String getIvString() {
        return KeyString.IvToString(keyMainKey.getIv());
    }

    public IvParameterSpec getIv() {
        return keyMainKey.getIv();
    };
}
