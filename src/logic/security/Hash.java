package logic.security;

import logic.data.Config;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * Hash class to hash a string with PBKDF2WithHmacSHA algorithm (512 for password and 256 for main key).
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class Hash {
    /**
     * Hash a string with PBKDF2WithHmacSHA algorithm (512 for password and 256 for main key).
     *
     * @param string string to hash
     * @param salt   salt to hash
     * @param mode   mode to hash (1 for password and 2 for main key)
     * @return Hashed string
     * @throws NoSuchAlgorithmException if the algorithm is not found
     * @throws InvalidKeySpecException  if the key is invalid
     * @author Reishandy (isthisruxury@gmail.com)
     * @see SecretKeyFactory
     * @see PBEKeySpec
     * @see Config#HASH_PASSWORD
     * @see Config#HASH_MAIN_KEY
     * @see Config#SIZE_ITERATION
     * @see Config#SIZE_AES
     * @see Encoder#encode(byte[])
     * @see Config#HASH_MODE_PASSWORD
     * @see Config#HASH_MODE_MAIN_KEY
     */
    public static String hash(String string, byte[] salt, int mode) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String algorithm = Config.HASH_PASSWORD;
        switch (mode) {
            case 1 -> algorithm = Config.HASH_PASSWORD;
            case 2 -> algorithm = Config.HASH_MAIN_KEY;
        }

        KeySpec spec = new PBEKeySpec(string.toCharArray(), salt, Config.SIZE_ITERATION, Config.SIZE_AES);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
        byte[] hash = factory.generateSecret(spec).getEncoded();

        return Encoder.encode(hash);
    }

    /**
     * Verify a password with a hashed password and salt.
     *
     * @param password       password in the form of string
     * @param hashedPassword hashed password in the form of string
     * @param salt           salt in the form of byte array
     * @return true if the password is correct, false otherwise
     * @throws InvalidKeySpecException  if the key is invalid
     * @throws NoSuchAlgorithmException if the algorithm is not found
     * @author Reishandy (isthisruxury@gmail.com)
     * @see Hash#hash(String, byte[], int)
     * @see Encoder#encode(byte[])
     * @see Config#HASH_MODE_PASSWORD
     * @see Config#HASH_MODE_MAIN_KEY
     */
    public static boolean verifyPassword(String password, String hashedPassword, byte[] salt) throws InvalidKeySpecException, NoSuchAlgorithmException {
        String newHashedPassword = hash(password, salt, Config.HASH_MODE_PASSWORD);
        return newHashedPassword.equals(hashedPassword);
    }
}
