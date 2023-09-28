package logic.storage;

import logic.data.User;
import logic.security.Crypto;
import logic.security.Hash;
import logic.security.KeyString;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;

public class UserDataBase {
    public static void initialization(String database) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DriverManager.getConnection(database);

            preparedStatement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS userdata (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "userID UNIQUE," +
                            "userName UNIQUE," +
                            "hashedPassword TEXT," +
                            "ivString TEXT," +
                            "saltString TEXT," +
                            "encryptedPrivateKeyString TEXT," +
                            "encryptedPublicKeyString TEXT);"
            );
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException(e);
        } finally {
            try {
                if (preparedStatement != null)
                    preparedStatement.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                throw new SQLException(e);
            }
        }
    }

    public static void addIntoDatabase(User user, String database) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DriverManager.getConnection(database);

            preparedStatement = connection.prepareStatement(
                    "INSERT OR IGNORE INTO userdata (" +
                            "userId, userName, hashedPassword, ivString, saltString, " +
                            "encryptedPrivateKeyString, encryptedPublicKeyString) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)"
            );
            preparedStatement.setString(1, user.getId());
            preparedStatement.setString(2, user.getUserName());
            preparedStatement.setString(3, user.getHashedPassword());
            preparedStatement.setString(4, user.getIvString());
            preparedStatement.setString(5, user.getSaltString());

            String encryptedPublicKey = Crypto.encryptAES(KeyString.PublicKeyToString(user.getPublicKey()), user.getMainKey(), user.getIv());
            String encryptedPrivateKey = Crypto.encryptAES(KeyString.PrivateKeyToString(user.getPrivateKey()), user.getMainKey(), user.getIv());
            preparedStatement.setString(6, encryptedPrivateKey);
            preparedStatement.setString(7, encryptedPublicKey);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException(e);
        } finally {
            try {
                if (preparedStatement != null)
                    preparedStatement.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                throw new SQLException(e);
            }
        }
    }

    public static User getUserFromDatabase(String userNameInput, String passwordInput, String database) throws SQLException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        User user = null;

        try {
            connection = DriverManager.getConnection(database);

            preparedStatement = connection.prepareStatement("SELECT * FROM userdata");
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String userId = resultSet.getString("userId");
                String userName = resultSet.getString("userName");
                String hashedPassword = resultSet.getString("hashedPassword");
                String ivString = resultSet.getString("ivString");
                String saltString = resultSet.getString("saltString");
                String encryptedPrivateKeyString = resultSet.getString("encryptedPrivateKeyString");
                String encryptedPublicKeyString = resultSet.getString("encryptedPublicKeyString");

                // This is not a good way to do it but i dont know what else
                // Straight checking which user is correct when getting the database
                if (userName.equals(userNameInput)) {
                    if (Hash.checkPassword(passwordInput, userNameInput, hashedPassword)) {
                        user = new User(userId, userName, passwordInput, hashedPassword, saltString, ivString, encryptedPublicKeyString, encryptedPrivateKeyString);
                    } else {
                        throw new BadPaddingException();
                    }
                }
            }
            return user;
        } catch (SQLException e) {
            throw new SQLException(e);
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                throw new SQLException(e);
            }
            passwordInput = null;
        }
    }

    public static boolean tableIsEmpty(String database) throws SQLException {
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
            throw new SQLException(e);
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                throw new SQLException(e);
            }
        }

        return rowCount == 0;
    }
}