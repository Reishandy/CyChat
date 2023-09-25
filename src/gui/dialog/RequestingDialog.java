package gui.dialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RequestingDialog extends JDialog {
    private JPanel contentPane;
    private JLabel messageLabel;
    private Timer timer;
    private int dotCount = 0;

    public RequestingDialog(JFrame frame) {
        // Set up
        super(frame, true);
        setContentPane(contentPane);
        setModal(false);

        // Displaying the request animation TODO
        startAnimation();

        // prevent closing by pressing the cross
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Sizing
        setSize(300, 200);
        setResizable(false);
    }

    private void startAnimation() {
        timer = new Timer(500, e -> {
            messageLabel.setText("Requesting" + ".".repeat(Math.max(0, dotCount)));

            dotCount++;
            if (dotCount > 3) dotCount = 0;
        });
        timer.start();
    }

    private void stopAnimation() {
        if (timer != null) {
            timer.stop();
        }
    }

    public void display() {
        setLocationRelativeTo(getParent());
        setVisible(true);
    }

    public void close() {
        stopAnimation();
        dispose();
    }
}
