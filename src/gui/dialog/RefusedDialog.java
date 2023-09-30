package gui.dialog;

import javax.swing.*;

public class RefusedDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel messageLabel;

    public RefusedDialog(JFrame frame, String message) {
        // Set up
        super(frame, true);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        messageLabel.setText(message);


        // Button handler
        buttonOK.addActionListener(e -> dispose());

        // prevent closing by pressing the cross
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Sizing
        setSize(300, 200);
        setResizable(false);
    }

    public void display() {
        setLocationRelativeTo(getParent());
        setVisible(true);
    }
}
