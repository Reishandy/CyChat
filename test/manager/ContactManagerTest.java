package manager;

import data.Constant;
import data.Contact;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import security.Crypto;
import security.KeyString;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class ContactManagerTest {
    private ContactManager contactManager;

    @BeforeEach
    public void setUp() {
        contactManager = new ContactManager();
    }

    @Test
    public void testAddContact() throws NoSuchAlgorithmException {
        Contact contact = new Contact("Alice", KeyString.PublicKeyToString(Crypto.generateRSAKey().getPublic()),
                KeyString.SecretKeyToString(Crypto.generateAESKey(Constant.keySizeAES128)), KeyString.IvToString(Crypto.generateIv()));
        contactManager.addContact(contact);
        assertTrue(contactManager.findContact(contact));
    }

    @Test
    public void testFindContact() throws NoSuchAlgorithmException {
        Contact contact = new Contact("Bob", KeyString.PublicKeyToString(Crypto.generateRSAKey().getPublic()),
                KeyString.SecretKeyToString(Crypto.generateAESKey(Constant.keySizeAES128)), KeyString.IvToString(Crypto.generateIv()));
        assertFalse(contactManager.findContact(contact));
        contactManager.addContact(contact);
        assertTrue(contactManager.findContact(contact));
    }

    @Test
    public void testGetContacts() throws NoSuchAlgorithmException {
        Contact contact1 = new Contact("Alice", KeyString.PublicKeyToString(Crypto.generateRSAKey().getPublic()),
                KeyString.SecretKeyToString(Crypto.generateAESKey(Constant.keySizeAES128)), KeyString.IvToString(Crypto.generateIv()));
        Contact contact2 = new Contact("bob", KeyString.PublicKeyToString(Crypto.generateRSAKey().getPublic()),
                KeyString.SecretKeyToString(Crypto.generateAESKey(Constant.keySizeAES128)), KeyString.IvToString(Crypto.generateIv()));
        contactManager.addContact(contact1);
        contactManager.addContact(contact2);
        assertEquals(2, contactManager.getContacts().size());
    }
}