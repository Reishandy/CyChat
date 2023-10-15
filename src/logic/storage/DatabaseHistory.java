package logic.storage;

import logic.data.user.Contact;
import logic.data.user.History;
import logic.data.user.User;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.ArrayList;

/**
 * DatabaseHistory class to store function used for database purposes related to history storage.
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class DatabaseHistory {
    /**
     * Function to store message history data into database, contact id is needed to store it into a seperated contact
     * table.
     *
     * @param contactId Contact's id
     * @param history Message data
     * @param databasePath Database path in string
     * @throws SQLException if table initialization failed
     * @author Reishandy (isthisruxury@gmail.com)
     * @see History
     * @see Database#tableInitialization(String, String)
     */
    public static void addHistoryIntoDatabase(String contactId, History history, String databasePath) throws SQLException {
        Database.tableInitialization(databasePath, contactId);

        String sql = "INSERT INTO " + contactId + """
                (user_id, username, date_time, message, message_iv)
                VALUES (?, ?, ?, ?, ?);
                """;

        try (Connection conn = DriverManager.getConnection(databasePath); PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, history.userId());
            preparedStatement.setString(2, history.username());
            preparedStatement.setString(3, history.dateTime());
            preparedStatement.setString(4, history.encryptedMessage());
            preparedStatement.setString(5, history.encodedIv());
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Get all history data from specific contact table that is identified by the contact id.
     *
     * @param contactId Contact's id
     * @param databasePath Database path in string
     * @return List of history
     * @throws SQLException if table initialization failed
     * @author Reishandy (isthisruxury@gmail.com)
     * @see History
     * @see Database#tableInitialization(String, String)
     */
    public static ArrayList<History> getHistoryFromDatabase(String contactId, String databasePath) throws SQLException {
        Database.tableInitialization(databasePath, contactId);

        String sql = "SELECT * FROM " + contactId + ";";

        try (Connection conn = DriverManager.getConnection(databasePath); PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            ArrayList<History> histories = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String userId = resultSet.getString("user_id");
                String username = resultSet.getString("username");
                String dateTime = resultSet.getString("date_time");
                String message = resultSet.getString("message");
                String messageIv = resultSet.getString("message_iv");

                histories.add(new History(userId, username, dateTime, message, messageIv));
            }

            return histories;
        } catch (SQLException e) {
            return null;
        }
    }
}
