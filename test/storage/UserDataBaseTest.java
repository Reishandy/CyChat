package storage;

import data.Constant;
import data.User;
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

    @BeforeEach @AfterEach
    void clear() throws SQLException {
        Connection connection = DriverManager.getConnection(Constant.databaseSQLite);
        Statement statement = connection.createStatement();
        statement.executeUpdate("DROP TABLE IF EXISTS userdata;");
        statement.close();
        connection.close();
    }

    @Test
    void testUserDataBase() {
        try {
            User user = new User("Cat", "I like cat");

            UserDataBase.initialization();
            UserDataBase.addIntoDatabase(user);
            User userGet = UserDataBase.getUserFromDatabase().get(0);

            assertNotNull(userGet);
            assertEquals(user.getUserName(), userGet.getUserName());
            assertEquals(user.getHashedPassword(), userGet.getHashedPassword());
            assertEquals(user.getSaltString(), userGet.getSaltString());
            assertEquals(user.getIvString(), userGet.getIvString());
            assertEquals(user.getPublicKey().hashCode(), userGet.getPublicKey().hashCode());
            assertEquals(user.getPrivateKey().hashCode(), userGet.getPrivateKey().hashCode());
            assertEquals(user.getMainKey().hashCode(), userGet.getMainKey().hashCode());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            fail("No errors should be thrown");
        }
    }
}