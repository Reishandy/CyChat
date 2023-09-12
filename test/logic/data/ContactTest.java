package logic.data;

import logic.security.Crypto;
import logic.security.KeyString;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class ContactTest {
    Contact contact;

    @Test
    void contactTest() {
        try {
            String userName = "Cat";
            String publicKeyString = KeyString.PublicKeyToString(Crypto.generateRSAKey().getPublic());
            String aesKeyString = KeyString.SecretKeyToString(Crypto.generateAESKey(Constant.keySizeAES128));
            String ivString = KeyString.IvToString(Crypto.generateIv());
            contact = new Contact(userName, publicKeyString, aesKeyString, ivString);

            assertNotNull(contact.getUserName());
            assertNotNull(contact.getPublicKeyString());
            assertNotNull(contact.getAESKeyString());
            assertNotNull(contact.getIvString());
            assertNotNull(contact.getIp());

            assertEquals(contact.getUserName(), userName);
            assertEquals(contact.getPublicKeyString(), publicKeyString);
            assertEquals(contact.getAESKeyString(), aesKeyString);
            assertEquals(contact.getIvString(), ivString);
            assertEquals(contact.getIp(), "192.168.0.0");

            ipTest();
        } catch (NoSuchAlgorithmException e) {
            fail("NoSuchAlgorithmException should not be thrown");
        }
    }

    void ipTest() {
        String ip = "192.168.1.1";
        contact.setIp(ip);

        assertNotNull(contact.getIp());
        assertEquals(contact.getIp(), ip);
    }
}