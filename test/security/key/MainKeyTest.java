package security.key;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import security.Hash;

import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;

class MainKeyTest {
    // I did not make this nor do i approve of this but im tired

    private MainKey mainKey;
    private final String password = "YourPassword";
    private final String userName = "user123";

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException, InvalidKeySpecException {
        mainKey = new MainKey(Hash.hashPassword(password, userName));
    }

    @Test
    void testGetKey() {
        SecretKey key = mainKey.getKey();
        assertNotNull(key);
    }

    @Test
    void testGetSaltString() {
        String saltString = mainKey.getSaltString();
        assertNotNull(saltString);
        assertFalse(saltString.isEmpty());
    }

    @Test
    void testConstructWithSalt() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String saltString = mainKey.getSaltString();
        MainKey mainKeyWithSalt = new MainKey(Hash.hashPassword(password, userName), saltString);

        assertEquals(saltString, mainKeyWithSalt.getSaltString());
        assertEquals(mainKey.getKey(), mainKeyWithSalt.getKey());
    }

}