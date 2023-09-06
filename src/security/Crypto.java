package security;

import data.Constant;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
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

    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[Constant.bitSizeIv];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static String encryptAES(String plainText, SecretKey key, IvParameterSpec iv) throws
            NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        Cipher cipher = Cipher.getInstance(Constant.algorithmAESCBC);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(cipherText);
    }

    public static String decryptAES(String cipherText, SecretKey key, IvParameterSpec iv) throws
            NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(Constant.algorithmAESCBC);
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
