package gui.bootup;

import com.formdev.flatlaf.FlatIntelliJLaf;
import gui.dialog.ErrorDialog;
import logic.data.Config;
import logic.data.user.User;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * Swing form class to handle the main frame of the program, and the boot-up sequence including splash screen.
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class Main {
    public static User user;
    public static JFrame mainFrame;
    private JPanel mainPanel;

    public static void main(String[] args) {
        // Set up theme
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
            UIManager.put( "ScrollBar.track", Config.SECONDARY_ACCENT_COLOR);
        } catch (
                UnsupportedLookAndFeelException e) {
            ErrorDialog errorDialog = new ErrorDialog(mainFrame, e);
            errorDialog.display();
        }

        // Set up application (JFrame)
        mainFrame = new JFrame("CyChat");
        mainFrame.setContentPane(new Main().mainPanel);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setPreferredSize(new Dimension(540, 720));
        mainFrame.setResizable(false);
        mainFrame.pack();
        mainFrame.setVisible(true);

        // Set up logo
        ImageIcon logoIconGif = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/CyChat.gif")));
        JLabel logoLabel = new JLabel(logoIconGif);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        mainFrame.getContentPane().setLayout(new GridBagLayout());
        mainFrame.getContentPane().add(logoLabel, constraints);
        mainFrame.getContentPane().setBackground(Color.WHITE);
        mainFrame.pack();

        // TODO: boot-up sequence, User authentication.
    }

    /**
     * Function to change the current panel of the main frame.
     *
     * @param panel The panel to be displayed.
     * @author Reishandy (isthisruxury@gmail.com)
     * @see JPanel
     * @see JFrame
     * @see Main
     * @see Main#mainFrame
     */
    public static void changePanel(JPanel panel) {
        panel.setBackground(mainFrame.getContentPane().getBackground());
        mainFrame.setContentPane(panel);
        mainFrame.repaint();
        mainFrame.revalidate();
    }

    public static JPanel getPanel() {
        return new Main().mainPanel;
    }
}
