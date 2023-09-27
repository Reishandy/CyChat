package gui.dialog;

import javax.swing.*;

public class RequestDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonAccept;
    private JButton buttonRefuse;
    private JLabel messageLabel;
    private boolean result;

    public RequestDialog(JFrame frame, String message) {
        // Set up
        super(frame, true);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonAccept);
        messageLabel.setText("<html>" + message.replace("\n", "<br>") + "</html>");

        // Button handler
        buttonAccept.addActionListener(e -> onOK());
        buttonRefuse.addActionListener(e -> onCancel());

        // prevent closing by pressing the cross
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

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
