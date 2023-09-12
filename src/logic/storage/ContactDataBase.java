package logic.storage;

import logic.data.Contact;
import logic.data.User;
import logic.security.Crypto;
import logic.security.KeyString;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;

public class ContactDataBase {
    public static void initialization(String database) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DriverManager.getConnection(database);

            preparedStatement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS contact (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "userName UNIQUE," +
                    "ip TEXT," +
                    "ivString TEXT," +
                    "encryptedAESKeyString TEXT," +
                    "encryptedPublicKeyString TEXT);"
            );
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (preparedStatement != null)
                    preparedStatement.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void addIntoDatabase(Contact contact, User user, String database) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DriverManager.getConnection(database);

            preparedStatement = connection.prepareStatement(
                    "INSERT OR IGNORE INTO contact (userName, ip, ivString, " +
                    "encryptedAESKeyString, encryptedPublicKeyString) VALUES (?, ?, ?, ?, ?)"
            );
            preparedStatement.setString(1, contact.getUserName());
            preparedStatement.setString(2, contact.getIp());
            preparedStatement.setString(3, contact.getIvString());

            String encryptedPublicKey = Crypto.encryptAES(KeyString.PublicKeyToString(contact.getPublicKey()), user.getMainKey(), user.getIv());
            String encryptedAESKey = Crypto.encryptAES(KeyString.SecretKeyToString(contact.getAESKey()), user.getMainKey(), user.getIv());
            preparedStatement.setString(4, encryptedAESKey);
            preparedStatement.setString(5, encryptedPublicKey);
            preparedStatement.executeUpdate();

        } catch (SQLException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (preparedStatement != null)
                    preparedStatement.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static ArrayList<Contact> getContactFromDatabase(User user, String database) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ArrayList<Contact> contacts = new ArrayList<>();

        try {
            connection = DriverManager.getConnection(database);

            preparedStatement = connection.prepareStatement("SELECT * FROM contact");
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String userName = resultSet.getString("userName");
                String ip = resultSet.getString("ip");
                String ivString = resultSet.getString("ivString");
                String encryptedAESKeyString = resultSet.getString("encryptedAESKeyString");
                String encryptedPublicKeyString = resultSet.getString("encryptedPublicKeyString");

                String aesKeyString = Crypto.decryptAES(encryptedAESKeyString, user.getMainKey(), user.getIv());
                String publicKeyString = Crypto.decryptAES(encryptedPublicKeyString, user.getMainKey(), user.getIv());

                Contact contact = new Contact(userName, publicKeyString, aesKeyString, ivString);
                contact.setIp(ip);
                contacts.add(contact);
            }

            return contacts;
        } catch (SQLException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                 IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
