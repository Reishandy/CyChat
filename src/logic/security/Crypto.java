package logic.security;

import logic.data.Config;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;

/**
 * Crypto class to encrypt and decrypt a string with AES and RSA algorithm.
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class Crypto {
    // Encryption

    /**
     * Encrypt a string with AES-256-CFB algorithm, Requires a key and IV and returns a ciphertext in Base64 string.
     *
     * @param plaintext plaintext to encrypt
     * @param key       key for encryption
     * @param iv        IV for encryption
     * @return Ciphertext in Base64 string
     * @throws NoSuchPaddingException             if the padding is not supported
     * @throws NoSuchAlgorithmException           if the algorithm is not supported
     * @throws InvalidAlgorithmParameterException if the algorithm parameter is invalid
     * @throws InvalidKeyException                if the key is invalid
     * @throws IllegalBlockSizeException          if the block size is invalid
     * @throws BadPaddingException                if the padding is invalid
     * @author Reishandy (isthisruxury@gmail.com)
     * @see Cipher
     * @see SecretKey
     * @see IvParameterSpec
     * @see Config#ALGORITHM_AES_CFB
     * @see Encoder#encode(byte[])
     * @see SecretKey
     * @see IvParameterSpec
     * @see Config#ALGORITHM_AES_CFB
     */
    public static String encryptAES(String plaintext, SecretKey key, IvParameterSpec iv) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        Cipher cipher = Cipher.getInstance(Config.ALGORITHM_AES_CFB);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
        return Encoder.encode(ciphertext);
    }

    /**
     * Decrypt a string with RSA-2048 algorithm, Requires a public key and returns a ciphertext in Base64 string.
     *
     * @param plaintext plaintext to encrypt
     * @param publicKey public key for encryption
     * @return Ciphertext in Base64 string
     * @throws NoSuchPaddingException    if the padding is not supported
     * @throws NoSuchAlgorithmException  if the algorithm is not supported
     * @throws InvalidKeyException       if the key is invalid
     * @throws IllegalBlockSizeException if the block size is invalid
     * @throws BadPaddingException       if the padding is invalid
     * @author Reishandy (isthisruxury@gmail.com)
     * @see Cipher
     * @see PublicKey
     * @see Config#ALGORITHM_RSA
     * @see Encoder#encode(byte[])
     */
    public static String encryptRSA(String plaintext, PublicKey publicKey) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(Config.ALGORITHM_RSA);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
        return Encoder.encode(ciphertext);
    }

    // Decryption

    /**
     * Decrypt a string with AES-256-CFB algorithm, Requires a key and IV and returns a plaintext in Base64 string.
     *
     * @param ciphertext ciphertext to decrypt
     * @param key        key for decryption
     * @param iv         IV for decryption
     * @return Plaintext
     * @throws NoSuchPaddingException             if the padding is not supported
     * @throws NoSuchAlgorithmException           if the algorithm is not supported
     * @throws InvalidAlgorithmParameterException if the algorithm parameter is invalid
     * @throws InvalidKeyException                if the key is invalid
     * @throws IllegalBlockSizeException          if the block size is invalid
     * @throws BadPaddingException                if the padding is invalid
     * @author Reishandy (isthisruxury@gmail.com)
     * @see Cipher
     * @see SecretKey
     * @see IvParameterSpec
     * @see Config#ALGORITHM_AES_CFB
     * @see Encoder#decode(String)
     */
    public static String decryptAES(String ciphertext, SecretKey key, IvParameterSpec iv) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        Cipher cipher = Cipher.getInstance(Config.ALGORITHM_AES_CFB);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] plaintext = cipher.doFinal(Encoder.decode(ciphertext));
        return new String(plaintext);
    }

    /**
     * Decrypt a string with RSA-2048 algorithm, Requires a private key and returns a plaintext in Base64 string.
     *
     * @param ciphertext ciphertext to decrypt
     * @param privateKey private key for decryption
     * @return Plaintext
     * @throws NoSuchPaddingException    if the padding is not supported
     * @throws NoSuchAlgorithmException  if the algorithm is not supported
     * @throws InvalidKeyException       if the key is invalid
     * @throws IllegalBlockSizeException if the block size is invalid
     * @throws BadPaddingException       if the padding is invalid
     * @author Reishandy (isthisruxury@gmail.com)
     * @see Cipher
     * @see PrivateKey
     * @see Config#ALGORITHM_RSA
     * @see Encoder#decode(String)
     */
    public static String decryptRSA(String ciphertext, PrivateKey privateKey) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(Config.ALGORITHM_RSA);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] plaintext = cipher.doFinal(Encoder.decode(ciphertext));
        return new String(plaintext);
    }
}
