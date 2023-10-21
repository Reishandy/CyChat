package gui.dialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Dialog class to handle and display error messages.
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class ErrorDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonExit;
    private JLabel errorTitle;
    private JLabel errorMessage;

    public ErrorDialog(JFrame frame, Exception throwable) {
        // Set up
        super(frame, true);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonExit);

        String message = getStackTraceError(throwable);
        if (message ==  null) message = "No message";
        errorMessage.setText("<html>" + message.replace("\n", "<br>") + "</html>");
        errorTitle.setText(throwable.getMessage() + " error occurred");

        buttonExit.addActionListener(e -> onExit());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });

        // Sizing
        setSize(400, 200);
    }

    private String getStackTraceError(Exception throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    private void onExit() {
        dispose();
        System.exit(1);
    }

    public void display() {
        setLocationRelativeTo(getParent());
        setVisible(true);
    }
}
