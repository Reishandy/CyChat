package logic.security.key;

import logic.security.Crypto;
import logic.security.KeyString;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;

public class RSATest {
    private RSA rsa;

    @Test
    public void testInitialization() {
        try {
            rsa = new RSA();
            assertNotNull(rsa.getPublicKey(), "Public key should not be null after initialization");
            assertNotNull(rsa.getPrivateKey(), "Private key should not be null after initialization");
        } catch (NoSuchAlgorithmException e) {
            fail("NoSuchAlgorithmException should not be thrown");
        }
    }

    @Test
    public void testInitializationWithString() {
        try {
            PublicKey publicKey = Crypto.generateRSAKey().getPublic();
            PrivateKey privateKey = Crypto.generateRSAKey().getPrivate();
            rsa = new RSA(KeyString.PublicKeyToString(publicKey), KeyString.PrivateKeyToString(privateKey));
            assertEquals(publicKey, rsa.getPublicKey(), "Setting public key string failed");
            assertEquals(privateKey, rsa.getPrivateKey(), "Setting private key string failed");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            fail("No error should not be thrown");
        }
    }
}
