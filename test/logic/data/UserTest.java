package logic.data;

import logic.security.Crypto;
import logic.security.Hash;
import logic.security.KeyString;
import logic.security.key.RSA;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private User user;

    @Test
    void testInitialization() {
        try {
            String userName = "Cat";
            String password = "I like cat";
            user = new User(userName, password);

            assertNotNull(user.getId());
            assertNotNull(user.getUserName());
            assertNotNull(user.getHashedPassword());
            assertNotNull(user.getIvString());
            assertNotNull(user.getPrivateKey());
            assertNotNull(user.getPublicKey());
            assertNotNull(user.getMainKey());
            assertNotNull(user.getSaltString());

            assertEquals(user.getHashedPassword(), Hash.hashPassword(password, userName));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            fail("Should not throw any error");
        }
    }

    @Test
    void testInitializationWithData() {
        try {
            String id = "CyChat_" + UUID.randomUUID().toString().replaceAll("-", "_");
            String userName = "Cat";
            String password = "I like cat";
            IvParameterSpec iv = Crypto.generateIv();
            byte[] salt = Crypto.generateSalt();

            SecretKey mainKey = Crypto.generateMainKey(password, salt);
            RSA rsa = new RSA();

            String encryptedPublicKey = Crypto.encryptAES(KeyString.PublicKeyToString(rsa.getPublicKey()), mainKey, iv);
            String encryptedPrivateKey = Crypto.encryptAES(KeyString.PrivateKeyToString(rsa.getPrivateKey()), mainKey, iv);

            user = new User(id, userName, password, Hash.hashPassword(userName, password), KeyString.SaltToString(salt), KeyString.IvToString(iv), encryptedPublicKey, encryptedPrivateKey);

            assertNotNull(user.getId());
            assertNotNull(user.getUserName());
            assertNotNull(user.getHashedPassword());
            assertNotNull(user.getIvString());
            assertNotNull(user.getPrivateKey());
            assertNotNull(user.getPublicKey());
            assertNotNull(user.getMainKey());
            assertNotNull(user.getSaltString());

            assertEquals(id, user.getId());
            assertEquals(user.getHashedPassword(), Hash.hashPassword(password, userName));
            assertEquals(user.getIvString(), KeyString.IvToString(iv));
            assertEquals(user.getSaltString(), KeyString.SaltToString(salt));
            assertEquals(KeyString.SecretKeyToString(user.getMainKey()), KeyString.SecretKeyToString(mainKey));
            assertEquals(KeyString.PublicKeyToString(user.getPublicKey()), KeyString.PublicKeyToString(rsa.getPublicKey()));
            assertEquals(KeyString.PrivateKeyToString(user.getPrivateKey()), KeyString.PrivateKeyToString(rsa.getPrivateKey()));

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidAlgorithmParameterException |
                 NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}