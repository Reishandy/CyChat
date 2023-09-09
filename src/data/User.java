package data;

import security.Crypto;
import security.Hash;
import security.KeyString;
import security.key.MainKey;
import security.key.RSA;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class User {
    private final String userName, hashedPassword;
    private final RSA keyRSA;
    private final MainKey keyMainKey;

    public User(String userName, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.userName = userName;
        this.hashedPassword = Hash.hashPassword(password, userName);
        this.keyRSA = new RSA();
        this.keyMainKey = new MainKey(password);
        password = null;
    }

    public User(String userName, String password, String hashedPassword, String saltString, String ivString, String encryptedPublicKey, String encryptedPrivateKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        this.userName = userName;
        this.hashedPassword = hashedPassword;
        this.keyMainKey = new MainKey(password, saltString, ivString);
        password = null;

        String publicKeyString = Crypto.decryptAES(encryptedPublicKey, getMainKey(), getIv());
        String privateKeyString = Crypto.decryptAES(encryptedPrivateKey, getMainKey(), getIv());
        this.keyRSA = new RSA(publicKeyString, privateKeyString);
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
