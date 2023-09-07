package security.key;

import data.Constant;
import security.Crypto;
import security.KeyString;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.NoSuchAlgorithmException;

public class AES {
    private SecretKey key;
    private IvParameterSpec iv;
    private String keyString, ivString;

    public void initialization() throws NoSuchAlgorithmException {
        key = Crypto.generateAESKey(Constant.keySizeAES128);
        iv = Crypto.generateIv();
        keyString = KeyString.SecretKeyToString(key);
        ivString = KeyString.IvToString(iv);
    }

    public void set(SecretKey key, IvParameterSpec iv) {
        this.key = key;
        this.iv = iv;
        keyString = KeyString.SecretKeyToString(key);
        ivString = KeyString.IvToString(iv);
    }

    public void set(String keyString, String ivString) {
        this.keyString = keyString;
        this.ivString = ivString;
        key = KeyString.StringToSecretKey(keyString);
        iv = KeyString.StringToIv(ivString);
    }

    public SecretKey getKey() {
        return key;
    }

    public IvParameterSpec getIv() {
        return iv;
    }

    public String getKeyString() {
        return keyString;
    }

    public String getIvString() {
        return ivString;
    }
}
