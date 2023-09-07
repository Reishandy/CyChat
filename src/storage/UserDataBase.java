package storage;

import data.Constant;
import data.User;
import security.Crypto;
import security.KeyString;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.ArrayList;

public class UserDataBase {
    public static void initialization() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DriverManager.getConnection(Constant.databaseSQLite);

            String createTableSQL = "CREATE TABLE IF NOT EXISTS userdata (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "userName UNIQUE," +
                    "hashedPassword TEXT," +
                    "ivString TEXT," +
                    "saltString TEXT," +
                    "encryptedPrivateKeyString TEXT," +
                    "encryptedPublicKeyString TEXT);";
            preparedStatement = connection.prepareStatement(createTableSQL);
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

    public static void addIntoDatabase(User user) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DriverManager.getConnection(Constant.databaseSQLite);

            String insertSQL = "INSERT OR IGNORE INTO userdata (userName, hashedPassword, ivString, saltString, " +
                    "encryptedPrivateKeyString, encryptedPublicKeyString) VALUES (?, ?, ?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(insertSQL);
            preparedStatement.setString(1, user.getUserName());
            preparedStatement.setString(2, user.getHashedPassword());
            preparedStatement.setString(3, user.getIvString());
            preparedStatement.setString(4, user.getSaltString());

            String encryptedPublicKey = Crypto.encryptAES(KeyString.PublicKeyToString(user.getPublicKey()), user.getMainKey(), KeyString.StringToIv(user.getIvString()));
            String encryptedPrivateKey = Crypto.encryptAES(KeyString.PrivateKeyToString(user.getPrivateKey()), user.getMainKey(), KeyString.StringToIv(user.getIvString()));
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

    public static ArrayList<User> getUserFromDatabase() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ArrayList<User> userList = new ArrayList<>();

        try {
            connection = DriverManager.getConnection(Constant.databaseSQLite);

            String selectSQL = "SELECT * FROM userdata";
            preparedStatement = connection.prepareStatement(selectSQL);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String userName = resultSet.getString("userName");
                String hashedPassword = resultSet.getString("hashedPassword");
                String ivString = resultSet.getString("ivString");
                String saltString = resultSet.getString("saltString");
                String encryptedPrivateKeyString = resultSet.getString("encryptedPrivateKeyString");
                String encryptedPublicKeyString = resultSet.getString("encryptedPublicKeyString");

                User user = new User(userName, hashedPassword, saltString, ivString, encryptedPublicKeyString, encryptedPrivateKeyString);
                userList.add(user);
            }

            return userList;
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException |
                 InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 BadPaddingException | InvalidKeyException e) {
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
}
