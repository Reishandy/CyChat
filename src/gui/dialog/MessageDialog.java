package gui.dialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MessageDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel messageLabel;

    public MessageDialog(JFrame frame, String message) {
        // Set up
        super(frame, true);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        messageLabel.setText(message);

        // Button handler
        buttonOK.addActionListener(e -> onOK());

        // prevent closing by pressing the cross
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Sizing
        setSize(300, 200);
        setResizable(false);
    }

    private void onOK() {
        dispose();
    }

    public void display() {
        setLocationRelativeTo(getParent());
        setVisible(true);
    }
}
