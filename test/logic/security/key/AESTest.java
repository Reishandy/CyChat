package logic.security.key;

import logic.data.Constant;
import logic.security.Crypto;
import logic.security.KeyString;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class AESTest {
    private AES aes;

    @Test
    void testInitialization() {
        try {
            aes = new AES();

            assertNotNull(aes.getIv());
            assertNotNull(aes.getIvString());
            assertEquals(KeyString.IvToString(aes.getIv()), aes.getIvString());

            assertNotNull(aes.getKey());
            assertNotNull(aes.getKeyString());
            assertNotNull(KeyString.SecretKeyToString(aes.getKey()), aes.getIvString());
        } catch (NoSuchAlgorithmException e) {
            fail("NoSuchAlgorithmException should not be thrown");
        }
    }

    @Test
    void testInitializationWithString() {
        try {
            String keyString = KeyString.SecretKeyToString(Crypto.generateAESKey(Constant.KEY_SIZE_AES_128));
            String ivString = KeyString.IvToString(Crypto.generateIv());

            aes = new AES(keyString, ivString);

            assertNotNull(aes.getKey());
            assertEquals(aes.getKeyString(), keyString);

            assertNotNull(aes.getIv());
            assertEquals(aes.getIvString(), ivString);
        } catch (NoSuchAlgorithmException e) {
            fail("NoSuchAlgorithmException should not be thrown");
        }
    }
}