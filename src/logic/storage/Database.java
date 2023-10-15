package logic.storage;

import logic.data.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Database class to store function used for general database purposes.
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class Database {
    /**
     * Get the database path in CyChat folder inside APPDATA folder and create the folder if it doesn't exist.
     *
     * @return Database path in String
     * @author Reishandy (isthisruxury@gmail.com)
     * @see Paths
     * @see File
     * @see Config#DATABASE_TYPE
     * @see Config#DATABASE_EXTENSION
     * @see Config#DATABASE_NAME
     */
    public static String getDatabasePath() throws IOException {
        String appDataCyChat = Paths.get(System.getenv("APPDATA"), "CyChat").toString();
        File appDataCyChatFile = new File(appDataCyChat);
        if (!appDataCyChatFile.exists()) {
            appDataCyChatFile.mkdirs();
        }

        return Config.DATABASE_TYPE + Paths.get(appDataCyChat + "/" + Config.DATABASE_NAME + Config.DATABASE_EXTENSION);
    }

    /**
     * Get the database path in CyChat folder inside APPDATA folder and create the folder if it doesn't exist. Additional
     * user id input to create a separate database for each user.
     *
     * @param id user id
     * @return Database path in String
     * @author Reishandy (isthisruxury@gmail.com)
     * @see Paths
     * @see File
     * @see Config#DATABASE_TYPE
     * @see Config#DATABASE_EXTENSION
     */
    public static String getDatabasePath(String id) {
        String appDataCyChat = Paths.get(System.getenv("APPDATA"), "CyChat").toString();
        File appDataCyChatFile = new File(appDataCyChat);
        if (!appDataCyChatFile.exists()) {
            appDataCyChatFile.mkdirs();
        }

        return Config.DATABASE_TYPE + Paths.get(appDataCyChat + "/" + id + Config.DATABASE_EXTENSION);
    }

    /**
     * Initialize the database with the table. support 3 mode: 1 for user table, 2 for contact table.
     * Mode 1 should be paired with the main database while the other mode should be paired with the user database.
     *
     * @param databasePath database path
     * @param mode         mode for table initialization
     * @author Reishandy (isthisruxury@gmail.com)
     * @see Connection
     * @see DriverManager
     * @see PreparedStatement
     * @see SQLException
     * @see Config#DATABASE_MODE_USER
     * @see Config#DATABASE_MODE_CONTACT
     */
    public static void tableInitialization(String databasePath, int mode) throws SQLException {
        String sql;
        switch (mode) {
            case 1 -> sql = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id TEXT NOT NULL UNIQUE,
                        username TEXT NOT NULL UNIQUE,
                        password TEXT,
                        password_salt TEXT,
                        main_key_salt TEXT,
                        public_key TEXT,
                        public_key_iv TEXT,
                        private_key TEXT,
                        private_key_iv TEXT
                    );
                    """;
            case 2 -> sql = """
                    CREATE TABLE IF NOT EXISTS contact (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id TEXT NOT NULL UNIQUE,
                        username TEXT,
                        chat_key TEXT,
                        chat_key_iv TEXT
                    );
                    """;
            default -> throw new SQLException();
        }

        try (Connection connection = DriverManager.getConnection(databasePath);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    public static void tableInitialization(String databasePath, String contactId) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + contactId + """
                    (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id TEXT,
                    username TEXT,
                    date_time TEXT,
                    message TEXT,
                    message_iv TEXT
                );
                """;

        try (Connection connection = DriverManager.getConnection(databasePath);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    /**
     * Check if the table is empty. support 3 mode: 1 for user table, 2 for contact table, 3 for history table.
     * Return true if the table is empty, false if the table is not empty, and false if the mode is invalid.
     * mode 1 should be paired with the main database while the other mode should be paired with the user database.
     *
     * @param databasePath database path
     * @param mode         mode for table initialization
     * @return true if the table is empty, false if the table is not empty, and false if the mode is invalid
     * @author Reishandy (isthisruxury@gmail.com)
     * @see Connection
     * @see DriverManager
     * @see PreparedStatement
     */
    public static boolean checkIfTableIsEmpty(String databasePath, int mode) {
        String sql;
        switch (mode) {
            case 1 -> sql = """
                    SELECT * FROM users;
                    """;
            case 2 -> sql = """
                    SELECT * FROM contact;
                    """;
            default -> {
                return false;
            }
        }

        try (Connection connection = DriverManager.getConnection(databasePath);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return !preparedStatement.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }
}
