package logic;

import logic.data.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import logic.security.Hash;
import logic.storage.DataBase;
import logic.storage.UserDataBase;

import java.io.IOException;
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
    void clear() throws SQLException, IOException {
        Connection connection = DriverManager.getConnection(DataBase.getDataBasePath());
        Statement statement = connection.createStatement();
        statement.executeUpdate("DROP TABLE IF EXISTS userdata;");
        statement.close();
        connection.close();
    }

    @Test
    void startSequence() {
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
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            fail("No errors should be thrown");
        }
    }

    private void login() {
        user = UserDataBase.getUserFromDatabase(userName, password, database);

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
