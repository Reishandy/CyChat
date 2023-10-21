package gui.dialog;

import javax.swing.*;
import java.awt.event.*;

public class LoadingDialog extends JDialog {
    private JPanel contentPane;
    private JLabel loadingLabel;
    private Timer timer;
    private int dotCount = 0;

    public LoadingDialog(JFrame frame) {
        // Set up
        super(frame, true);
        setContentPane(contentPane);
        setModal(false);

        // Displaying the request animation
        startAnimation();

        // prevent closing by pressing the cross
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Sizing
        setSize(300, 200);
        setResizable(false);
    }

    private void startAnimation() {
        timer = new Timer(500, e -> {
            loadingLabel.setText("Loading" + ".".repeat(Math.max(0, dotCount)));

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
