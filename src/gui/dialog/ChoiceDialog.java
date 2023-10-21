package gui.dialog;

import javax.swing.*;

/**
 * Dialog class to handle decision making.
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class ChoiceDialog extends JDialog {
    private boolean result = false;
    private JPanel contentPane;
    private JButton buttonAccept;
    private JButton buttonRefuse;
    private JLabel messageLabel;

    public ChoiceDialog(JFrame frame, String message) {
        // Set up
        super(frame, true);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonAccept);
        messageLabel.setText("<html>" + message.replace("\n", "<br>") + "</html>");

        // Button handler
        buttonAccept.addActionListener(e -> onAccept());
        buttonRefuse.addActionListener(e -> onRefuse());

        // prevent closing by pressing the cross
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Sizing
        setSize(300, 200);
        setResizable(false);
    }

    private void onAccept() {
        result = true;
        dispose();
    }

    private void onRefuse() {
        result = false;
        dispose();
    }

    public boolean getResult() {
        return result;
    }

    public void display() {
        setLocationRelativeTo(getParent());
        setVisible(true);
    }
}
