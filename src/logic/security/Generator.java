package logic.security;

import logic.data.Config;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * Generator class to generate AES and RSA keys, IV, and Salt.
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class Generator {
    /**
     * Generate a random IV for encryption, must be generated for each new plaintext
     *
     * @return IvParameterSpec object (basically the IV)
     * @throws NoSuchAlgorithmException if the algorithm is not supported
     * @author Reishandy (isthisruxury@gmail.com)
     * @see IvParameterSpec
     * @see SecureRandom
     * @see Config#SIZE_SUPPORT
     */
    public static IvParameterSpec generateIV() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstanceStrong();
        byte[] iv = new byte[Config.SIZE_SUPPORT];
        random.nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    /**
     * Generate a random salt for hashing, must be generated for each new plaintext
     *
     * @return byte array of the salt
     * @throws NoSuchAlgorithmException if the algorithm is not supported
     * @author Reishandy (isthisruxury@gmail.com)
     * @see SecureRandom
     * @see Config#SIZE_SUPPORT
     */
    public static byte[] generateSalt() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstanceStrong();
        byte[] salt = new byte[Config.SIZE_SUPPORT];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Generate an AES-256 key with KeyGenerator.
     *
     * @return SecretKey object (basically the key)
     * @throws NoSuchAlgorithmException if the algorithm is not supported
     * @author Reishandy (isthisruxury@gmail.com)
     * @see KeyGenerator
     * @see SecretKey
     * @see Config#ALGORITHM_AES
     * @see Config#SIZE_AES
     */
    public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(Config.ALGORITHM_AES);
        keyGenerator.init(Config.SIZE_AES);
        return keyGenerator.generateKey();
    }

    /**
     * Generate an RSA-2048 key pair with KeyPairGenerator, PrivateKey is used for decryption and PublicKey is used
     * for encryption.
     *
     * @return KeyPair object (PrivateKey and PublicKey)
     * @throws NoSuchAlgorithmException if the algorithm is not supported
     * @author Reishandy (isthisruxury@gmail.com)
     * @see KeyPairGenerator
     * @see KeyPair
     * @see Config#ALGORITHM_RSA
     * @see Config#SIZE_RSA
     */
    public static KeyPair generateRSAKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(Config.ALGORITHM_RSA);
        keyPairGenerator.initialize(Config.SIZE_RSA);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Generate a main key from a password and salt, Used to secure user's Public and Private Key.
     *
     * @param password The password to derive the main key
     * @param salt     The salt to generate the main key
     * @return SecretKey object (basically the main key)
     * @throws NoSuchAlgorithmException If the algorithm is not supported
     * @throws InvalidKeySpecException  If the key is invalid
     * @author Reishandy (isthisruxury@gmail.com)
     * @see Hash#hash(String, byte[], int)
     * @see Encoder#decodeAESKey(String)
     * @see Config#HASH_MODE_MAIN_KEY
     * @see SecretKey
     */
    public static SecretKey generateMainKey(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String derivative = Hash.hash(password, salt, Config.HASH_MODE_MAIN_KEY);
        return Encoder.decodeAESKey(derivative);
    }
}
