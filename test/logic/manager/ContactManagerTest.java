package logic.manager;

import logic.data.Constant;
import logic.data.Contact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import logic.security.Crypto;
import logic.security.KeyString;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ContactManagerTest {
    private ContactManager contactManager;

    @BeforeEach
    public void setUp() {
        contactManager = new ContactManager();
    }

    @Test
    public void testCheckContactExist() throws NoSuchAlgorithmException {
        Contact contact1 = new Contact("User1", "User1", KeyString.PublicKeyToString(Crypto.generateRSAKey().getPublic()),
                KeyString.SecretKeyToString(Crypto.generateAESKey(Constant.keySizeAES128)), KeyString.IvToString(Crypto.generateIv()));
        contactManager.addContact(contact1);

        assertTrue(contactManager.checkContactExist("User1"));
        assertFalse(contactManager.checkContactExist("User2"));
    }

    @Test
    public void testAddContact() throws NoSuchAlgorithmException {
        Contact contact1 = new Contact("User1","User1", KeyString.PublicKeyToString(Crypto.generateRSAKey().getPublic()),
                KeyString.SecretKeyToString(Crypto.generateAESKey(Constant.keySizeAES128)), KeyString.IvToString(Crypto.generateIv()));
        contactManager.addContact(contact1);

        assertEquals(1, contactManager.getContacts().size());
        assertTrue(contactManager.checkContactExist("User1"));
    }

    @Test
    public void testUpdateIpAddress() throws NoSuchAlgorithmException {
        Contact contact1 = new Contact("User1","User1", KeyString.PublicKeyToString(Crypto.generateRSAKey().getPublic()),
                KeyString.SecretKeyToString(Crypto.generateAESKey(Constant.keySizeAES128)), KeyString.IvToString(Crypto.generateIv()));
        contactManager.addContact(contact1);

        contactManager.updateIpAddress("User1", "192.168.0.2");

        Contact updatedContact = contactManager.getContact("User1");
        assertNotNull(updatedContact);
        assertEquals("192.168.0.2", updatedContact.getIp());
    }

    @Test
    public void testGetContact() throws NoSuchAlgorithmException {
        Contact contact1 = new Contact("User1","User1", KeyString.PublicKeyToString(Crypto.generateRSAKey().getPublic()),
                KeyString.SecretKeyToString(Crypto.generateAESKey(Constant.keySizeAES128)), KeyString.IvToString(Crypto.generateIv()));
        contactManager.addContact(contact1);

        Contact retrievedContact = contactManager.getContact("User1");

        assertNotNull(retrievedContact);
        assertEquals("User1", retrievedContact.getUserName());
        assertEquals("192.168.0.0", retrievedContact.getIp());
    }
}