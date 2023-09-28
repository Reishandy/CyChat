package logic.storage;

import logic.data.History;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HistoryDataBaseTest {
    String database, recipient, recipientId, senderId;

    @BeforeEach
    @AfterEach
    void clear() throws SQLException {
        database = "jdbc:sqlite:test.db";
        recipient = "Cat";
        recipientId = "CyChat_" + UUID.randomUUID().toString().replaceAll("-", "_");
        senderId = "CyChat_" + UUID.randomUUID().toString().replaceAll("-", "_");

        Connection connection = DriverManager.getConnection(database);
        PreparedStatement statement = connection.prepareStatement("DROP TABLE IF EXISTS " + recipientId);
        statement.executeUpdate();
        statement.close();
        connection.close();
    }

    @Test
    void testHistoryDataBase() throws SQLException {
        String sender = "Dog";
        String message1 = "I like cats";
        String message2 = "I hate cats";
        String time = "23/09/10 - 23:23";

        ArrayList<History> histories = new ArrayList<>();
        histories.add(new History(recipient, time, message1));
        histories.add(new History(sender, time, message2));

        HistoryDataBase.initialization(recipientId, database);
        HistoryDataBase.addIntoDatabase(recipientId, histories, database);

        ArrayList<History> historiesGet = HistoryDataBase.getHistoryFromDatabase(recipientId, database);
        assertNotNull(historiesGet);
        assertEquals(2, historiesGet.size());

        History history1 = historiesGet.get(0);
        History history2 = historiesGet.get(1);

        assertEquals(recipient, history1.userName());
        assertEquals(time, history1.dateTime());
        assertEquals(message1, history1.message());

        assertEquals(sender, history2.userName());
        assertEquals(time, history2.dateTime());
        assertEquals(message2, history2.message());
    }
}