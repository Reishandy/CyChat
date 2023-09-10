package storage;

import data.Constant;
import data.User;
import security.Crypto;
import security.Hash;
import security.KeyString;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;

public class UserDataBase {
    public static void initialization(String database) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DriverManager.getConnection(database);

            preparedStatement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS userdata (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "userName UNIQUE," +
                            "hashedPassword TEXT," +
                            "ivString TEXT," +
                            "saltString TEXT," +
                            "encryptedPrivateKeyString TEXT," +
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

    public static void addIntoDatabase(User user, String database) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DriverManager.getConnection(database);

            preparedStatement = connection.prepareStatement(
                    "INSERT OR IGNORE INTO userdata (" +
                            "userName, hashedPassword, ivString, saltString, " +
                            "encryptedPrivateKeyString, encryptedPublicKeyString) " +
                            "VALUES (?, ?, ?, ?, ?, ?)"
            );
            preparedStatement.setString(1, user.getUserName());
            preparedStatement.setString(2, user.getHashedPassword());
            preparedStatement.setString(3, user.getIvString());
            preparedStatement.setString(4, user.getSaltString());

            String encryptedPublicKey = Crypto.encryptAES(KeyString.PublicKeyToString(user.getPublicKey()), user.getMainKey(), user.getIv());
            String encryptedPrivateKey = Crypto.encryptAES(KeyString.PrivateKeyToString(user.getPrivateKey()), user.getMainKey(), user.getIv());
            preparedStatement.setString(5, encryptedPrivateKey);
            preparedStatement.setString(6, encryptedPublicKey);
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

    public static User getUserFromDatabase(String userNameInput, String passwordInput, String database) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        User user = null;

        try {
            connection = DriverManager.getConnection(database);

            preparedStatement = connection.prepareStatement("SELECT * FROM userdata");
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String userName = resultSet.getString("userName");
                String hashedPassword = resultSet.getString("hashedPassword");
                String ivString = resultSet.getString("ivString");
                String saltString = resultSet.getString("saltString");
                String encryptedPrivateKeyString = resultSet.getString("encryptedPrivateKeyString");
                String encryptedPublicKeyString = resultSet.getString("encryptedPublicKeyString");

                // This is not a good way to do it but i dont know what else
                // Straight checking which user is correct when getting the database
                if (userName.equals(userNameInput) && Hash.checkPassword(passwordInput, userNameInput, hashedPassword)) {
                    user = new User(userName, passwordInput, hashedPassword, saltString, ivString, encryptedPublicKeyString, encryptedPrivateKeyString);
                }
            }
            return user;
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException |
                 InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            passwordInput = null;
        }
    }

    public static boolean tableIsEmpty(String database) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int rowCount = 0;

        try {
            connection = DriverManager.getConnection(database);
            String selectQuery = "SELECT COUNT(*) FROM userdata";
            preparedStatement = connection.prepareStatement(selectQuery);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                rowCount = resultSet.getInt(1);
            }
        } catch (SQLException e) {
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

        return rowCount == 0;
    }
}