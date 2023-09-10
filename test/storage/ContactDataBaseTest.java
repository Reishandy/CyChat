package storage;

import data.Constant;
import data.Contact;
import data.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import security.Crypto;
import security.KeyString;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
            String userName = "Dog";
            String ivString = KeyString.IvToString(Crypto.generateIv());
            String aesKeyString = KeyString.SecretKeyToString(Crypto.generateAESKey(Constant.keySizeAES128));
            String publicKeyString = KeyString.PublicKeyToString(Crypto.generateRSAKey().getPublic());

            User user = new User("Cat", "I like cat");
            Contact contact = new Contact(userName, publicKeyString, aesKeyString, ivString);

            ContactDataBase.initialization(database);
            ContactDataBase.addIntoDatabase(contact, user, database);

            Contact contactGet = ContactDataBase.getContactFromDatabase(user, database).get(0);

            assertNotNull(contactGet);
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