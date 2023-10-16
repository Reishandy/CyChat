package logic.storage;

import logic.data.user.History;

import java.sql.*;
import java.util.ArrayList;

/**
 * DatabaseHistory class to store function used for database purposes related to history storage.
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class DatabaseHistory {
    /**
     * Add history into database. This function will encrypt the history's message using AES algorithm and then store
     * it into the database with it's iv. This function will return 0 if the history is successfully added into the
     * database and 1 if the history already exist. This function will also create a table for the contact if it doesn't
     * exist. This function will also create a database for the contact if it doesn't exist.
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
     * Get history from database. This function will return a list of history from the database. This function will also
     * create a table for the contact if it doesn't exist. This function will also create a database for the contact if
     * it doesn't exist.
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
