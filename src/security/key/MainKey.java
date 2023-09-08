package security.key;

import security.Crypto;
import security.KeyString;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class MainKey {
    private final SecretKey key;
    private final byte[] salt;
    private final IvParameterSpec iv;
    private final String saltString, ivString;

    public MainKey(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        salt = Crypto.generateSalt();
        iv = Crypto.generateIv();
        key = Crypto.generateMainKey(password, salt);
        saltString = KeyString.SaltToString(salt);
        ivString = KeyString.IvToString(iv);
        password = null;
    }

    public MainKey(String password, String saltString, String ivString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.saltString = saltString;
        this.ivString = ivString;
        this.salt = KeyString.StringToSalt(saltString);
        this.iv = KeyString.StringToIv(ivString);
        key = Crypto.generateMainKey(password, this.salt);
        password = null;
    }

    public SecretKey getKey() {
        return key;
    }

    public String getSaltString() {
        return saltString;
    }

    public String getIvString() {
        return ivString;
    }

    public IvParameterSpec getIv() {
        return iv;
    }
}
