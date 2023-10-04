package gui.bootup;

import com.formdev.flatlaf.FlatIntelliJLaf;
import gui.chat.ChatGUI;
import gui.dialog.Error;
import logic.data.Constant;
import logic.data.User;
import logic.manager.ContactManager;
import logic.manager.PeerManager;
import logic.network.ChatReceiver;
import logic.network.ChatSender;
import logic.storage.DataBase;
import logic.storage.UserDataBase;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class SplashScreen {
    public static User user;
    public static JFrame frame;
    private JPanel splashScreen;

    public static Thread broadcastListenerThread = null;
    public static Thread exchangeListenerThread = null;
    public static Thread chatListenerThread = null;
    public static ChatSender chatSender = null;
    public static ChatReceiver chatReceiver = null;
    public static ContactManager contactManager = null;
    public static PeerManager peerManager = null;

    public static void main(String[] args) throws IOException, InterruptedException {
        // Set up theme
        setUpTheme();

        // Set up application (JFrame)
        frame = new JFrame("CyChat");
        frame.setContentPane(new SplashScreen().splashScreen);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(540, 720));
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);

        // Load fonts
        loadFonts();

        // Set up logo
        ImageIcon logoIconGif = new ImageIcon(Objects.requireNonNull(SplashScreen.class.getResource("/CyChat.gif")));
        JLabel logoLabel = new JLabel(logoIconGif);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        frame.getContentPane().setLayout(new GridBagLayout());
        frame.getContentPane().add(logoLabel, constraints);
        frame.getContentPane().setBackground(Color.WHITE);
        frame.pack();

        // boot up sequence
        boolean tableEmpty = true;
        try {
            UserDataBase.initialization(DataBase.getDataBasePath());
            tableEmpty = UserDataBase.tableIsEmpty(DataBase.getDataBasePath());
        } catch (SQLException e) {
            Error dialog = new Error(SplashScreen.frame, e);
            dialog.display();
        }

        Thread.sleep(5000);

        if (tableEmpty) {
            changePanel(Register.getRegister());
        } else {
            changePanel(Login.getLogin());
        }
    }

    private static void loadFonts() {
        try {
            Font barlowExtraBold = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(SplashScreen.class.getResourceAsStream("/Barlow-ExtraBold.ttf")));
            Font barlowMedium = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(SplashScreen.class.getResourceAsStream("/Barlow-Medium.ttf")));

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(barlowExtraBold);
            ge.registerFont(barlowMedium);
        } catch (FontFormatException | IOException e) {
            Error dialog = new Error(frame, e);
            dialog.display();
        }
    }

    private static void setUpTheme() {
        try {
            UIManager.setLookAndFeel( new FlatIntelliJLaf() );
            UIManager.put( "Button.arc", 999 );
            UIManager.put( "Component.arc", 999 );
            UIManager.put( "ProgressBar.arc", 999 );
            UIManager.put( "TextComponent.arc", 999 );
            UIManager.put( "Component.arrowType", "triangle" );
            UIManager.put( "Component.focusWidth", 3 );
            UIManager.put( "ScrollBar.trackArc", 999 );
            UIManager.put( "ScrollBar.thumbArc", 999 );
            UIManager.put( "ScrollBar.trackInsets", new Insets( 2, 4, 2, 4 ) );
            UIManager.put( "ScrollBar.thumbInsets", new Insets( 2, 2, 2, 2 ) );
            UIManager.put( "ScrollBar.track", Constant.SECONDARY_ACCENT_COLOR);
        } catch (
                UnsupportedLookAndFeelException e) {
            Error dialog = new Error(frame, e);
            dialog.display();
        }
    }

    public static void changePanel(JPanel panel) {
        panel.setBackground(frame.getContentPane().getBackground());
        frame.setContentPane(panel);
        frame.repaint();
        frame.revalidate();
    }

    public static JPanel getSplashScreen() {
        return new SplashScreen().splashScreen;
    }
}
