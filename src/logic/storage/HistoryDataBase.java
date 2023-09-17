package logic.storage;

import logic.data.History;

import java.sql.*;
import java.util.ArrayList;

public class HistoryDataBase {
    public static void initialization(String id, String database) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DriverManager.getConnection(database);

            if (!id.matches("[A-Za-z0-9_]+")) {
                throw new SQLException("Invalid table name");
            }

            preparedStatement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + id + " (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "userName UNIQUE," +
                            "dateTime TEXT," +
                            "encryptedMessage TEXT);"
            );
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (preparedStatement != null)
                    preparedStatement.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void addIntoDatabase(String id, ArrayList<History> histories, String database) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DriverManager.getConnection(database);

            if (!id.matches("[A-Za-z0-9_]+")) {
                throw new SQLException("Invalid table name");
            }
            preparedStatement = connection.prepareStatement(
                    "INSERT OR IGNORE INTO " + id + " (userName, dateTime, encryptedMessage) VALUES (?, ?, ?)"
            );

            for (History history: histories) {
                preparedStatement.setString(1, history.userName());
                preparedStatement.setString(2, history.dateTime());
                preparedStatement.setString(3, history.message());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (preparedStatement != null)
                    preparedStatement.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static ArrayList<History> getHistoryFromDatabase(String id, String database) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ArrayList<History> histories = new ArrayList<>();

        try {
            connection = DriverManager.getConnection(database);

            if (!id.matches("[A-Za-z0-9_]+")) {
                throw new SQLException("Invalid table name");
            }
            preparedStatement = connection.prepareStatement("SELECT * FROM " + id);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String userNameDB = resultSet.getString("userName");
                String dateTime = resultSet.getString("dateTime");
                String encryptedMessage = resultSet.getString("encryptedMessage");

                History history = new History(userNameDB, dateTime, encryptedMessage);
                histories.add(history);
            }

            return histories;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
