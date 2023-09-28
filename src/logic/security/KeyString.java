package logic.security;

import logic.data.Constant;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyString {
    public static String SecretKeyToString(SecretKey key) {
        byte[] rawKey = key.getEncoded();
        return Base64.getEncoder().encodeToString(rawKey);
    }

    public static SecretKey StringToSecretKey(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, Constant.ALGORITHM_AES);
    }

    public static String IvToString(IvParameterSpec iv) {
        byte[] rawIv = iv.getIV();
        return Base64.getEncoder().encodeToString(rawIv);
    }

    public static IvParameterSpec StringToIv(String encodedIv) {
        byte[] decodedIv = Base64.getDecoder().decode(encodedIv);
        return new IvParameterSpec(decodedIv);
    }

    public static String SaltToString(byte[] salt) {
        return Base64.getEncoder().encodeToString(salt);
    }

    public static byte[] StringToSalt(String encodedSalt) {
        return Base64.getDecoder().decode(encodedSalt);
    }

    public static String PublicKeyToString(PublicKey key) {
        byte[] rawKey = key.getEncoded();
        return Base64.getEncoder().encodeToString(rawKey);
    }

    public static PublicKey StringToPublicKey(String encodedKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance(Constant.ALGORITHM_RSA);
        return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
    }

    public static String PrivateKeyToString(PrivateKey key) {
        byte[] rawKey = key.getEncoded();
        return Base64.getEncoder().encodeToString(rawKey);
    }

    public static PrivateKey StringToPrivateKey(String encodedKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance(Constant.ALGORITHM_RSA);
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodedKey));
    }
}
