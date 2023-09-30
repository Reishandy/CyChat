package logic.data;

import logic.security.key.AES;
import logic.security.key.RSA;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

public class Contact {
    private final String id, userName;
    private String ip;
    private final RSA keyRSA;
    private final AES keyAES;

    public Contact(String id, String userName, String publicKeyString, String aesKeyString, String ivString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.id = id;
        this.ip = "NONE";
        this.userName = userName;
        this.keyRSA = new RSA(publicKeyString);
        this.keyAES = new AES(aesKeyString, ivString);
    }

    public Contact(String id, String userName, String ip, String publicKeyString, String aesKeyString, String ivString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.id = id;
        this.ip = ip;
        this.userName = userName;
        this.keyRSA = new RSA(publicKeyString);
        this.keyAES = new AES(aesKeyString, ivString);
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public String getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getPublicKeyString() {
        return keyRSA.getPublicKeyString();
    }

    public String getAESKeyString() {
        return keyAES.getKeyString();
    }

    public String getIvString() {
        return keyAES.getIvString();
    }

    public PublicKey getPublicKey() {
        return keyRSA.getPublicKey();
    }

    public SecretKey getAESKey() {
        return keyAES.getKey();
    }

    public IvParameterSpec getIv() {
        return keyAES.getIv();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact contact)) return false;

        if (!Objects.equals(userName, contact.userName)) return false;
        if (!Objects.equals(ip, contact.ip)) return false;
        if (!Objects.equals(keyRSA, contact.keyRSA)) return false;
        return Objects.equals(keyAES, contact.keyAES);
    }

    @Override
    public int hashCode() {
        int result = userName != null ? userName.hashCode() : 0;
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + keyRSA.hashCode();
        result = 31 * result + keyAES.hashCode();
        return result;
    }
}
