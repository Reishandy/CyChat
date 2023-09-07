import data.Constant;
import data.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import security.Hash;
import storage.UserDataBase;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class FullLoginSequenceTest {
    User user;
    String userName = "Cat";
    String password = "I like cats";
    ArrayList<User> userList;

    @BeforeEach
    @AfterEach
    void clear() throws SQLException {
        Connection connection = DriverManager.getConnection(Constant.databaseSQLite);
        Statement statement = connection.createStatement();
        statement.executeUpdate("DROP TABLE IF EXISTS userdata;");
        statement.close();
        connection.close();
    }

    @Test
    void startSequence() {
        UserDataBase.initialization();
        userList = UserDataBase.getUserFromDatabase();

        if (userList.isEmpty()) {
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

            UserDataBase.addIntoDatabase(userA);
            login();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            fail("No errors should be thrown");
        }
    }

    private void login() {
        userList = UserDataBase.getUserFromDatabase();

        userList.forEach((userA) -> {
            try {
                if (userA.getUserName().equals(userName) && userA.getHashedPassword().equals(Hash.hashPassword(password, userName))) {
                    user = userA;
                }
            } catch (NoSuchAlgorithmException e) {
                fail("NoSuchAlgorithmException should not be thrown");
            }
        });

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
