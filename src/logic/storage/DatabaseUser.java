package logic.storage;

import logic.data.Config;
import logic.data.user.User;
import logic.security.Crypto;
import logic.security.Encoder;
import logic.security.Generator;
import logic.security.Hash;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;

/**
 * DatabaseUser class to store function used for database purposes related to user storage.
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class DatabaseUser {
    /**
     * Add user into database. This function will encrypt the user's public key and private key using AES algorithm and
     * then store it into the database with it's iv. This function will also store the user's hashed password and
     * password salt into the database. This function will return 0 if the user is successfully added into the database
     * and 1 if the user already exist.
     *
     * @param user         User object
     * @param databasePath Database path in String
     * @return 0 if the user is successfully added into the database and 1 if the user already exist
     * @throws NoSuchAlgorithmException   if the algorithm is not found
     * @throws InvalidAlgorithmParameterException  if the algorithm parameter is invalid
     * @throws NoSuchPaddingException    if the padding is not found
     * @throws IllegalBlockSizeException if the block size is illegal
     * @throws BadPaddingException      if the padding is bad
     * @throws InvalidKeyException     if the key is invalid
     * @author Reishandy (isthisruxury@gmail.com)
     * @see User
     * @see Crypto#encryptAES(String, SecretKey, IvParameterSpec)
     * @see Encoder#encode(byte[])
     * @see Generator#generateIV()
     * @see DriverManager
     * @see Connection
     * @see PreparedStatement
     * @see SQLException
     */
    public static int addUserIntoDatabase(User user, String databasePath) throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException, SQLException {
        Database.tableInitialization(databasePath, Config.DATABASE_MODE_USER);

        String sql = """
                INSERT INTO users (
                user_id, username, password, password_salt, main_key_salt,
                public_key, public_key_iv, private_key, private_key_iv
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;

        IvParameterSpec publicKeyIv = Generator.generateIV();
        IvParameterSpec privateKeyIv = Generator.generateIV();
        String encryptedPublicKey = Crypto.encryptAES(Encoder.encode(user.getPublicKey().getEncoded()), user.getMainKey(), publicKeyIv);
        String encryptedPrivateKey = Crypto.encryptAES(Encoder.encode(user.getPrivateKey().getEncoded()), user.getMainKey(), privateKeyIv);

        try (Connection conn = DriverManager.getConnection(databasePath); PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, user.getUserId());
            preparedStatement.setString(2, user.getUsername());
            preparedStatement.setString(3, user.getHashedPassword());
            preparedStatement.setString(4, user.getEncodedPasswordSalt());
            preparedStatement.setString(5, user.getEncodedMainKeySalt());
            preparedStatement.setString(6, encryptedPublicKey);
            preparedStatement.setString(7, Encoder.encode(publicKeyIv.getIV()));
            preparedStatement.setString(8, encryptedPrivateKey);
            preparedStatement.setString(9, Encoder.encode(privateKeyIv.getIV()));
            preparedStatement.executeUpdate();
            return 0;
        } catch (SQLException e) {
            return 1;
        }
    }

    /**
     *  Get user from database. This function will return User object if the user is found in the database and null if
     *  the user is not found in the database. This function will also decrypt the user's public key and private key
     *  using AES algorithm. This function will throw BadPaddingException if the password is wrong.
     *
     * @param username Username
     * @param password Password, for verification
     * @param databasePath Database path in String
     * @return User object if the user is found in the database and null if the user is not found in the database
     * @throws BadPaddingException if the password is wrong
     * @throws InvalidKeySpecException if the key spec is invalid
     * @throws NoSuchAlgorithmException if the algorithm is not found
     * @throws InvalidAlgorithmParameterException if the algorithm parameter is invalid
     * @throws NoSuchPaddingException if the padding is not found
     * @throws IllegalBlockSizeException if the block size is illegal
     * @throws InvalidKeyException if the key is invalid
     * @author Reishandy (isthisruxury@gmail.com)
     * @see User
     * @see Hash#verifyPassword(String, String, byte[])
     * @see Crypto#decryptAES(String, SecretKey, IvParameterSpec)
     * @see Encoder#decodeIV(String)
     * @see DriverManager
     * @see Connection
     * @see PreparedStatement
     * @see ResultSet
     */
    public static User getUserFromDatabase(String username, String password, String databasePath) throws BadPaddingException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidKeyException, SQLException {
        Database.tableInitialization(databasePath, Config.DATABASE_MODE_USER);

        String sql = """
                SELECT * FROM users;
                """;

        try (Connection conn = DriverManager.getConnection(databasePath); PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String usernameFromDatabase = resultSet.getString("username");
                if (!usernameFromDatabase.equals(username)) {
                    continue;
                }

                String hashedPasswordFromDatabase = resultSet.getString("password");
                String passwordSaltFromDatabase = resultSet.getString("password_salt");
                if (!Hash.verifyPassword(password, hashedPasswordFromDatabase, Encoder.decode(passwordSaltFromDatabase))) {
                    throw new BadPaddingException("Wrong password");
                }

                String userId = resultSet.getString("user_id");
                String mainKeySaltFromDatabase = resultSet.getString("main_key_salt");
                String publicKeyFromDatabase = resultSet.getString("public_key");
                String publicKeyIvFromDatabase = resultSet.getString("public_key_iv");
                String privateKeyFromDatabase = resultSet.getString("private_key");
                String privateKeyIvFromDatabase = resultSet.getString("private_key_iv");

                SecretKey mainKey = Generator.generateMainKey(password, Encoder.decode(mainKeySaltFromDatabase));
                String decryptedPublicKey = Crypto.decryptAES(publicKeyFromDatabase, mainKey, Encoder.decodeIV(publicKeyIvFromDatabase));
                String decryptedPrivateKey = Crypto.decryptAES(privateKeyFromDatabase, mainKey, Encoder.decodeIV(privateKeyIvFromDatabase));

                return new User(userId, username, password, hashedPasswordFromDatabase, passwordSaltFromDatabase, mainKeySaltFromDatabase, decryptedPublicKey, decryptedPrivateKey);
            }
        } catch (SQLException e) {
            return null;
        }
        return null;
    }
}
