package logic.storage;

import logic.data.Constant;
import logic.data.Contact;
import logic.data.User;
import logic.security.Crypto;
import logic.security.KeyString;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ContactDataBaseTest {
    String database;

    @BeforeEach
    @AfterEach
    void clear() throws SQLException {
        database = "jdbc:sqlite:test.db";
        Connection connection = DriverManager.getConnection(database);
        Statement statement = connection.createStatement();
        statement.executeUpdate("DROP TABLE IF EXISTS contact;");
        statement.close();
        connection.close();
    }

    @Test
    void testContactDataBase() {
        try {
            String id = "CyChat_" + UUID.randomUUID().toString().replaceAll("-", "_");
            String userName = "Dog";
            String ivString = KeyString.IvToString(Crypto.generateIv());
            String aesKeyString = KeyString.SecretKeyToString(Crypto.generateAESKey(Constant.KEY_SIZE_AES_128));
            String publicKeyString = KeyString.PublicKeyToString(Crypto.generateRSAKey().getPublic());

            User user = new User("Cat", "I like cat");
            Contact contact = new Contact(id, userName, publicKeyString, aesKeyString, ivString);

            ContactDataBase.initialization(database);
            ContactDataBase.addIntoDatabase(contact, user, database);

            Contact contactGet = ContactDataBase.getContactFromDatabase(user, database).get(0);

            assertNotNull(contactGet);
            assertEquals(contactGet.getId(), contact.getId());
            assertEquals(contactGet.getUserName(), contact.getUserName());
            assertEquals(contactGet.getIp(), contact.getIp());
            assertEquals(contactGet.getAESKeyString(), contact.getAESKeyString());
            assertEquals(contactGet.getIvString(), contact.getIvString());
            assertEquals(contactGet.getPublicKeyString(), contact.getPublicKeyString());

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            fail("No errors should be thrown");
        }

    }
}