package logic.security;

import logic.data.Config;

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

/**
 * Encoder class to encode and decode byte arrays from AES and RSA key, IV, and Salt into Base64 string.
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class Encoder {
    /**
     * Encode an array of bytes into Base64 string, support for AES and RSA keys, IV, Salt, and Hashing.
     *
     * @param bytes byte array to encode
     * @return Encoded byte arrays in Base64 string
     * @author Reishandy (isthisruxury@gmail.com)
     * @see Base64
     */
    public static String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Decode a String encoded in Base64 into a byte array.
     *
     * @param encodedString Base64 string to decode
     * @return Decoded byte array
     * @author Reishandy (isthisruxury@gmail.com)
     * @see Base64
     */
    public static byte[] decode(String encodedString) {
        return Base64.getDecoder().decode(encodedString);
    }

    /**
     * Decode a Base64 encoded IV into an IvParameterSpec object (basically the IV).
     *
     * @param encodedIV Base64 string IV to decode
     * @return Decoded IV
     * @author Reishandy (isthisruxury@gmail.com)
     * @see Base64
     * @see IvParameterSpec
     */
    public static IvParameterSpec decodeIV(String encodedIV) {
        byte[] decodedIV = Base64.getDecoder().decode(encodedIV);
        return new IvParameterSpec(decodedIV);
    }

    /**
     * Decode a Base64 encoded AES key into a SecretKey object (basically the AES key).
     *
     * @param encodedKey Base64 string key to decode
     * @return Decoded AES key
     * @author Reishandy (isthisruxury@gmail.com)
     * @see Base64
     * @see SecretKey
     * @see SecretKeySpec
     * @see Config#ALGORITHM_AES
     */
    public static SecretKey decodeAESKey(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, Config.ALGORITHM_AES);
    }

    /**
     * Decode a Base64 encoded RSA key into a PublicKey object (basically the RSA PublicKey).
     *
     * @param encodedKey Base64 string public key to decode
     * @return Decoded RSA public key
     * @throws NoSuchAlgorithmException if the RSA algorithm is not found
     * @throws InvalidKeySpecException  if the key is invalid
     * @author Reishandy (isthisruxury@gmail.com)
     * @see Base64
     * @see PublicKey
     * @see X509EncodedKeySpec
     * @see KeyFactory
     * @see Config#ALGORITHM_RSA
     */
    public static PublicKey decodeRSAPublicKey(String encodedKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance(Config.ALGORITHM_RSA);
        return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
    }

    /**
     * Decode a Base64 encoded RSA key into a PrivateKey object (basically the RSA PrivateKey).
     *
     * @param encodedKey Base64 string private key to decode
     * @return Decoded RSA private key
     * @throws NoSuchAlgorithmException if the RSA algorithm is not found
     * @throws InvalidKeySpecException  if the key is invalid
     * @author Reishandy (isthisruxury@gmail.com)
     * @see Base64
     * @see PrivateKey
     * @see PKCS8EncodedKeySpec
     * @see KeyFactory
     * @see Config#ALGORITHM_RSA
     */
    public static PrivateKey decodeRSAPrivateKey(String encodedKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance(Config.ALGORITHM_RSA);
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodedKey));
    }
}
