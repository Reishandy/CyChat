package logic.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributeView;

public class DataBase {
    public static String getDataBasePath(String userName) throws IOException {
        String appDataCyChat = Paths.get(System.getenv("APPDATA"), "CyChat").toString();
        File appDataDir = new File(appDataCyChat);
        if (!appDataDir.exists()) {
            appDataDir.mkdirs();
        }

        DosFileAttributeView attributes = Files.getFileAttributeView(
                new File(appDataCyChat).toPath(), DosFileAttributeView.class
        );
        attributes.setHidden(true);

        return "jdbc:sqlite:" + Paths.get(appDataCyChat, userName + ".db");
    }

    public static String getDataBasePath() throws IOException {
        String appDataCyChat = Paths.get(System.getenv("APPDATA"), "CyChat").toString();
        File appDataDir = new File(appDataCyChat);
        if (!appDataDir.exists()) {
            appDataDir.mkdirs();
        }

        DosFileAttributeView attributes = Files.getFileAttributeView(
                new File(appDataCyChat).toPath(), DosFileAttributeView.class
        );
        attributes.setHidden(true);

        return "jdbc:sqlite:" + Paths.get(appDataCyChat, "CyChat.db");
    }
}
