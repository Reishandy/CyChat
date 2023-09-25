package logic.security;

import logic.data.Constant;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class Crypto {
    public static SecretKey generateAESKey(int bit) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(Constant.algorithmAES);
        keyGenerator.init(bit);
        return keyGenerator.generateKey();
    }

    public static KeyPair generateRSAKey() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(Constant.algorithmRSA);
        generator.initialize(Constant.keySizeRSA);
        return generator.generateKeyPair();
    }

    public static SecretKey generateMainKey(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, Constant.mainKeyIteration, Constant.keySizeAES256);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        SecretKey secretKey = keyFactory.generateSecret(keySpec);
        return new SecretKeySpec(secretKey.getEncoded(), "AES");
    }

    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[Constant.bit16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static byte[] generateSalt() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstanceStrong();
        byte[] salt = new byte[Constant.bit16];
        random.nextBytes(salt);
        return salt;
    }

    public static String encryptAES(String plainText, SecretKey key, IvParameterSpec iv) throws
            NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        Cipher cipher = Cipher.getInstance(Constant.algorithmAESCFB);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(cipherText);
    }

    public static String decryptAES(String cipherText, SecretKey key, IvParameterSpec iv) throws
            NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(Constant.algorithmAESCFB);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(plainText);
    }

    public static String encryptRSA(PublicKey publicKey, String plainText) throws
            NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {

        Cipher encryptCipher = Cipher.getInstance(Constant.algorithmRSA);
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] cipherTextBytes = encryptCipher.doFinal(plainTextBytes);
        return Base64.getEncoder().encodeToString(cipherTextBytes);
    }

    public static String decryptRSA(PrivateKey privateKey, String cipherText) throws
            NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {

        Cipher decryptCipher = Cipher.getInstance(Constant.algorithmRSA);
        decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] plainText = decryptCipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(plainText);
    }
}
