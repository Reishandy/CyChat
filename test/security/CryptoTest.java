package security;

import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CryptoTest {

    @Test
    public void generateAESKey() {
        try {
            SecretKey key = Crypto.generateAESKey(128);
            assertNotNull(key, "Generated key should not be null");
            assertEquals("AES", key.getAlgorithm(), "Generated key should be an AES key");
        } catch (NoSuchAlgorithmException e) {
            fail("NoSuchAlgorithmException should not be thrown");
        }
    }

    @Test
    void generateRSAKey() {
        try {
            KeyPair keyPair = Crypto.generateRSAKey();
            assertNotNull(keyPair, "Generated key should not be null");
            assertEquals("RSA", keyPair.getPrivate().getAlgorithm(), "Generated private key should be an AES key");
            assertEquals("RSA", keyPair.getPrivate().getAlgorithm(), "Generated public key should be an AES key");
        } catch (NoSuchAlgorithmException e) {
            fail("NoSuchAlgorithmException should not be thrown");
        }
    }

    @Test
    void generateMainKey() {
        try {
            String password = "ILIKECATS123";
            byte[] salt = Crypto.generateSalt();
            SecretKey mainKey = Crypto.generateMainKey(password, salt);
            SecretKey mainKeyDup = Crypto.generateMainKey(password, salt);
            assertNotNull(mainKey, "Generated key should not be null");
            assertEquals("AES", mainKey.getAlgorithm(), "Generated key should be an AES key");
            assertEquals(Arrays.toString(mainKeyDup.getEncoded()), Arrays.toString(mainKey.getEncoded()),
                    "Generated key with the same password and salt must have the same value");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            fail("There should not be any error thrown");
        }
    }

    @Test
    void generateIv() {
        IvParameterSpec ivParameterSpec = Crypto.generateIv();
        assertNotNull(ivParameterSpec, "Generated iv should not be null");
    }

    @Test
    void generateSalt() {
        try {
            byte[] salt = Crypto.generateSalt();
            assertNotNull(salt, "Generated salt should not be null");
        } catch (NoSuchAlgorithmException e) {
            fail("NoSuchAlgorithmException should not be thrown");
        }
    }

    @Test
    void encryptDecryptAES() {
        try {
            SecretKey key = Crypto.generateAESKey(128);
            IvParameterSpec ivParameterSpec = Crypto.generateIv();
            String plainText = "I like cats";
            String cipherText = Crypto.encryptAES(plainText, key, ivParameterSpec);
            String decryptedText = Crypto.decryptAES(cipherText, key, ivParameterSpec);
            assertEquals(plainText, decryptedText, "Text should be equals");
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                 IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            fail("There should not be any error thrown");
        }
    }

    @Test
    void encryptDecryptRSA() {
        try {
            KeyPair keyPair = Crypto.generateRSAKey();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();
            String plainText = "I like cats";
            String cipherText = Crypto.encryptRSA(publicKey, plainText);
            String decryptedText = Crypto.decryptRSA(privateKey, cipherText);
            assertEquals(plainText, decryptedText, "Text should be equals");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                 IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            fail("There should not be any error thrown");
        }
    }

    @Test
    void encryptDecryptMainKey() throws InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        try {
            SecretKey keyToSave = Crypto.generateAESKey(128);
            SecretKey mainKey = Crypto.generateMainKey("ILIKECATS123", Crypto.generateSalt());
            IvParameterSpec iv = Crypto.generateIv();

            String encryptedKey = Crypto.encryptAES(KeyString.SecretKeyToString(keyToSave), mainKey, iv);
            String decryptedKey = Crypto.decryptAES(encryptedKey, mainKey, iv);

            SecretKey decryptedKeyToSave = KeyString.StringToSecretKey(decryptedKey);
            assertEquals(Arrays.toString(keyToSave.getEncoded()), Arrays.toString(decryptedKeyToSave.getEncoded()),
                    "Keys should be the same after encryption and decryption with MainKey");
        } catch (NoSuchAlgorithmException e) {
            fail("There should not be any error thrown");
        }
    }
 }