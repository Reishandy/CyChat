package logic.storage;

import logic.data.Config;
import logic.data.user.Contact;
import logic.data.user.User;
import logic.security.Crypto;
import logic.security.Encoder;
import logic.security.Generator;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;

/**
 * DatabaseContact class to store function used for database purposes related to contact storage.
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class DatabaseContact {
    /**
     * Add contact into database. This function will encrypt the contact's chat key using AES algorithm and then store
     * it into the database with it's iv. This function will return 0 if the contact is successfully added into the
     * database and 1 if the contact already exist.
     *
     * @param user       User object from current user
     * @param contact   Contact object from the contact to be added
     * @param databasePath Database path in String
     * @return 0 if the contact is successfully added into the database and 1 if the contact already exist
     * @throws NoSuchAlgorithmException if the algorithm is not found
     * @throws InvalidAlgorithmParameterException if the algorithm parameter is invalid
     * @throws NoSuchPaddingException if the padding is not found
     * @throws IllegalBlockSizeException if the block size is illegal
     * @throws BadPaddingException if the padding is bad
     * @throws InvalidKeyException if the key is invalid
     * @author Reishandy (isthisruxury@gmail.com)
     * @see User
     * @see Contact
     * @see Crypto#encryptAES(String, SecretKey, IvParameterSpec)
     * @see Encoder#encode(byte[])
     * @see Generator#generateIV()
     * @see DriverManager
     * @see Connection
     * @see PreparedStatement
     */
    public static int addContactIntoDatabase(User user, Contact contact, String databasePath) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, SQLException {
        Database.tableInitialization(databasePath, Config.DATABASE_MODE_CONTACT);

        String sql = """
                INSERT INTO contact (user_id, username, chat_key, chat_key_iv)
                VALUES (?, ?, ?, ?);
                """;

        IvParameterSpec iv = Generator.generateIV();
        String encryptedChatKey = Crypto.encryptAES(contact.getEncodedKeyChatAES(), user.getMainKey(), iv);

        try (Connection conn = DriverManager.getConnection(databasePath); PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, contact.getUserId());
            preparedStatement.setString(2, contact.getUsername());
            preparedStatement.setString(3, encryptedChatKey);
            preparedStatement.setString(4, Encoder.encode(iv.getIV()));
            preparedStatement.executeUpdate();
            return 0;
        } catch (SQLException e) {
            return 1;
        }
    }

    /**
     * Get contact from database. This function will decrypt the contact's chat key using AES algorithm and then store
     * it into the database with it's iv. This function will return an ArrayList of Contact object if the contact is
     * successfully retrieved from the database and null if the contact is not found.
     *
     * @param user User object from current user
     * @param databasePath Database path in String
     * @return an ArrayList of Contact object if the contact is successfully retrieved from the database and null if the contact is not found
     * @throws InvalidAlgorithmParameterException if the algorithm parameter is invalid
     * @throws NoSuchPaddingException if the padding is not found
     * @throws IllegalBlockSizeException if the block size is illegal
     * @throws NoSuchAlgorithmException if the algorithm is not found
     * @throws BadPaddingException if the padding is bad
     * @throws InvalidKeyException if the key is invalid
     * @author Reishandy (isthisruxury@gmail.com)
     * @see User
     * @see Contact
     * @see Crypto#decryptAES(String, SecretKey, IvParameterSpec)
     * @see Encoder#decodeIV(String)
     * @see DriverManager
     * @see Connection
     * @see PreparedStatement
     * @see ResultSet
     */
    public static ArrayList<Contact> getContactFromDatabase(User user, String databasePath) throws
            InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
            BadPaddingException, InvalidKeyException, SQLException {
        Database.tableInitialization(databasePath, Config.DATABASE_MODE_CONTACT);

        String sql = """
                SELECT * FROM contact;
                """;

        try (Connection conn = DriverManager.getConnection(databasePath); PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            ArrayList<Contact> contacts = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String userId = resultSet.getString("user_id");
                String username = resultSet.getString("username");
                String encryptedChatKey = resultSet.getString("chat_key");
                String encodedChatKeyIv = resultSet.getString("chat_key_iv");

                String decryptedChatKey = Crypto.decryptAES(encryptedChatKey, user.getMainKey(), Encoder.decodeIV(encodedChatKeyIv));
                contacts.add(new Contact(userId, username, decryptedChatKey));
            }
            return contacts;
        } catch (SQLException e) {
            return null;
        }
    }
}
