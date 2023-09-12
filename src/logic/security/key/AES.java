package logic.security.key;

import logic.data.Constant;
import logic.security.Crypto;
import logic.security.KeyString;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.NoSuchAlgorithmException;

public class AES {
    private final SecretKey key;
    private final IvParameterSpec iv;
    private final String keyString, ivString;

    public AES() throws NoSuchAlgorithmException {
        key = Crypto.generateAESKey(Constant.keySizeAES128);
        iv = Crypto.generateIv();
        keyString = KeyString.SecretKeyToString(key);
        ivString = KeyString.IvToString(iv);
    }

    public AES(String keyString, String ivString) {
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
