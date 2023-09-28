package logic;

import logic.data.User;
import logic.security.Hash;
import logic.storage.UserDataBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class FullLoginSequenceTest {
    User user;
    String userName = "Cat";
    String password = "I like cats";
    String database = "jdbc:sqlite:test.db";

    @BeforeEach
    @AfterEach
    void clear() throws SQLException {
        Connection connection = DriverManager.getConnection(database);
        Statement statement = connection.createStatement();
        statement.executeUpdate("DROP TABLE IF EXISTS userdata;");
        statement.close();
        connection.close();
    }

    @Test
    void startSequence() throws SQLException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        UserDataBase.initialization(database);

        if (UserDataBase.tableIsEmpty(database)) {
            register();
        } else {
            login(); // Not reachable, only for flow demonstration
        }
    }

    private void register() {
        try {
            User userA = new User(userName, password);

            assertNotNull(userA.getId());
            assertNotNull(userA.getUserName());
            assertNotNull(userA.getHashedPassword());
            assertNotNull(userA.getIvString());
            assertNotNull(userA.getPrivateKey());
            assertNotNull(userA.getPublicKey());
            assertNotNull(userA.getMainKey());
            assertNotNull(userA.getSaltString());
            assertEquals(userA.getUserName(), userName);
            assertEquals(userA.getHashedPassword(), Hash.hashPassword(password, userName));

            UserDataBase.addIntoDatabase(userA, database);
            login();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidAlgorithmParameterException |
                 NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
                 SQLException e) {
            fail("No errors should be thrown");
        }
    }

    private void login() throws InvalidAlgorithmParameterException, SQLException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        user = UserDataBase.getUserFromDatabase(userName, password, database);

        assertNotNull(user.getId());
        assertNotNull(user.getUserName());
        assertNotNull(user.getHashedPassword());
        assertNotNull(user.getIvString());
        assertNotNull(user.getPrivateKey());
        assertNotNull(user.getPublicKey());
        assertNotNull(user.getMainKey());
        assertNotNull(user.getSaltString());
        assertEquals(user.getUserName(), userName);
        try {
            assertEquals(user.getHashedPassword(), Hash.hashPassword(password, userName));
        } catch (NoSuchAlgorithmException e) {
            fail("NoSuchAlgorithmException should not be thrown");
        }
    }
}
