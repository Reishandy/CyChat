package storage;

import data.Constant;
import data.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class UserDataBaseTest {

    @BeforeEach
    @AfterEach
    void clear() throws SQLException, IOException {
        Connection connection = DriverManager.getConnection(DataBase.getDataBasePath());
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

            UserDataBase.initialization();
            UserDataBase.addIntoDatabase(user);
            User userGet = UserDataBase.getUserFromDatabase(userName, password);

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