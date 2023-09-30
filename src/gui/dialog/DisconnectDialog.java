package gui.dialog;

import javax.swing.*;
import java.awt.event.*;

public class DisconnectDialog extends JDialog {
    private boolean result;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel messageLabel;

    public DisconnectDialog(JFrame frame) {
        super(frame, true);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonCancel);

        messageLabel.setText("Do you want to disconnect?");

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // Sizing
        setSize(300, 200);
        setResizable(false);
    }

    private void onOK() {
        result = true;
        dispose();
    }

    private void onCancel() {
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
