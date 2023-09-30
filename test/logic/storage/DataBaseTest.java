package logic.storage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataBaseTest {
    private static final String TEST_APPDATA = "test_appdata";

    @BeforeAll
    public static void setUp() {
        // Create a temporary test AppData directory
        String testAppDataPath = Paths.get(System.getProperty("java.io.tmpdir"), TEST_APPDATA).toString();
        File testAppDataDir = new File(testAppDataPath);
        if (!testAppDataDir.exists()) {
            testAppDataDir.mkdirs();
        }
    }

    @AfterAll
    public static void tearDown() throws IOException {
        String testAppDataPath = Paths.get(System.getProperty("java.io.tmpdir"), TEST_APPDATA).toString();
        File testAppDataDir = new File(testAppDataPath);
        if (testAppDataDir.exists()) {
            Files.deleteIfExists(testAppDataDir.toPath());
        }
    }

    @Test
    public void testGetDataBasePath() throws IOException {
        System.setProperty("APPDATA", Paths.get(System.getProperty("java.io.tmpdir"), TEST_APPDATA).toString());

        String databasePath = DataBase.getDataBasePath();

        assertNotNull(databasePath);
        assertTrue(databasePath.startsWith("jdbc:sqlite"));
        assertTrue(databasePath.endsWith("CyChat.db"));
    }

    @Test
    public void testGetDataBasePathWithUserName() throws IOException {
        String userName = "Cat";
        System.setProperty("APPDATA", Paths.get(System.getProperty("java.io.tmpdir"), TEST_APPDATA).toString());

        String databasePath = DataBase.getDataBasePath(userName);

        assertNotNull(databasePath);
        assertTrue(databasePath.startsWith("jdbc:sqlite"));
        assertTrue(databasePath.endsWith(userName + ".db"));
    }
}