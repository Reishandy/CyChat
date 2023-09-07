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
    private final IvParameterSpec iv;
    private final RSA keyRSA;
    private final MainKey keyMainKey;

    public User(String userName, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.userName = userName;
        this.hashedPassword = Hash.hashPassword(password, userName);
        this.iv = Crypto.generateIv();
        this.keyRSA = new RSA();
        this.keyMainKey = new MainKey(this.hashedPassword);
    }

    public User(String userName, String password, String saltString, String ivString, String encryptedPublicKey, String encryptedPrivateKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        this.userName = userName;
        this.hashedPassword = Hash.hashPassword(password, userName);
        this.iv = KeyString.StringToIv(ivString);
        this.keyMainKey = new MainKey(hashedPassword, saltString);

        String publicKeyString = Crypto.decryptAES(encryptedPublicKey, getMainKey(), iv);
        String privateKeyString = Crypto.decryptAES(encryptedPrivateKey, getMainKey(), iv);
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
        return KeyString.IvToString(iv);
    }
}
