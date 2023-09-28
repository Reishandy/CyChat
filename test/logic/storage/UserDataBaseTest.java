package logic.storage;

import logic.data.User;
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

class UserDataBaseTest {
    String database;

    @BeforeEach
    @AfterEach
    void clear() throws SQLException {
        database = "jdbc:sqlite:test.db";
        Connection connection = DriverManager.getConnection(database);
        Statement statement = connection.createStatement();
        statement.executeUpdate("DROP TABLE IF EXISTS userdata;");
        statement.close();
        connection.close();
    }

    @Test
    void testUserDataBase() {
        try {
            String userName = "Cat";
            String password = "I like cat";
            User user = new User(userName, password);

            UserDataBase.initialization(database);
            UserDataBase.addIntoDatabase(user, database);
            User userGet = UserDataBase.getUserFromDatabase(userName, password, database);

            assertNotNull(userGet);
            assertEquals(user.getId(), userGet.getId());
            assertEquals(user.getUserName(), userGet.getUserName());
            assertEquals(user.getHashedPassword(), userGet.getHashedPassword());
            assertEquals(user.getSaltString(), userGet.getSaltString());
            assertEquals(user.getIvString(), userGet.getIvString());
            assertEquals(user.getPublicKey().hashCode(), userGet.getPublicKey().hashCode());
            assertEquals(user.getPrivateKey().hashCode(), userGet.getPrivateKey().hashCode());
            assertEquals(user.getMainKey().hashCode(), userGet.getMainKey().hashCode());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | SQLException |
                 InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 BadPaddingException | InvalidKeyException e) {
            fail("No errors should be thrown");
        }
    }
}