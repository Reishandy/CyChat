package logic.security;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class KeyStringTest {

    @Test
    void secretKey() {
        try {
            SecretKey secretKey = Crypto.generateAESKey(128);
            String secretKeyString = KeyString.SecretKeyToString(secretKey);
            SecretKey secretKeyDecoded = KeyString.StringToSecretKey(secretKeyString);
            assertEquals(secretKey, secretKeyDecoded, "Round-trip testing failed for SecretKey");
        } catch (NoSuchAlgorithmException e) {
            fail("NoSuchAlgorithmException should not be thrown");
        }
    }

    @Test
    void iv() {
        IvParameterSpec iv = Crypto.generateIv();
        String ivString = KeyString.IvToString(iv);
        IvParameterSpec ivDecoded = KeyString.StringToIv(ivString);
        assertEquals(Arrays.toString(ivDecoded.getIV()), Arrays.toString(iv.getIV()), "Round-trip testing failed for IV");
    }

    @Test
    void publicKey() {
        try {
            PublicKey publicKey = Crypto.generateRSAKey().getPublic();
            String publicKeyString = KeyString.PublicKeyToString(publicKey);
            PublicKey publicKeyDecoded = KeyString.StringToPublicKey(publicKeyString);
            assertEquals(publicKeyDecoded, publicKey, "Round-trip testing failed for PublicKey");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            fail("No error should not be thrown");
        }
    }

    @Test
    void privateKey() {
        try {
            PrivateKey privateKey = Crypto.generateRSAKey().getPrivate();
            String privateKeyString = KeyString.PrivateKeyToString(privateKey);
            PrivateKey privateKeyDecoded = KeyString.StringToPrivateKey(privateKeyString);
            assertEquals(privateKeyDecoded, privateKey, "Round-trip testing failed for PrivateKey");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            fail("No error should not be thrown");
        }
    }
}