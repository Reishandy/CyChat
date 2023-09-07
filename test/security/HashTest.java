package security;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class HashTest {
    @Test
    void hashTrue() {
        String uname = "Cats";
        String password = "I like cats 123";

        try {
            String hashedPassword = Hash.hashPassword(password, uname);
            assertTrue(Hash.checkPassword("I like cats 123", "Cats", hashedPassword),
                    "Hash should match with the same uname and passwd");
        } catch (NoSuchAlgorithmException e) {
            fail("NoSuchAlgorithmException should not be thrown");
        }
    }

    @Test
    void hashFalse() {
        String uname = "Cats";
        String password = "I like cats 123";
        String unameFalse = "Dogs";
        String passwordFalse = "I like dogs 123";

        try {
            String hashedPassword = Hash.hashPassword(password, uname);
            assertFalse(Hash.checkPassword(unameFalse, passwordFalse,hashedPassword),
                    "Hash should not match given different uname and passwd");
        } catch (NoSuchAlgorithmException e) {
            fail("NoSuchAlgorithmException should not be thrown");
        }
    }
}