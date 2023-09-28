package gui.dialog;

import javax.swing.*;
import java.awt.event.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Error extends JDialog {
    private JPanel contentPane;
    private JButton buttonExit;
    private JLabel errorMessageLabel;
    private JLabel errorTitleTable;

    public Error(JFrame frame, Exception throwable) {
        // Set up
        super(frame, true);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonExit);
        setSize(400, 200);


        String message = getStackTraceError(throwable);
        if (message ==  null) message = "No message";
        errorMessageLabel.setText("<html>" + message.replace("\n", "<br>") + "</html>");
        errorTitleTable.setText(throwable.getMessage() + " error occurred");

        buttonExit.addActionListener(e -> onExit());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });
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

    public static void main(String[] args) {
        try {
            int o = 5 / 0;
        } catch (Exception e) {
            Error dialog = new Error(new JFrame(), e);
            dialog.display();
            System.exit(0);
        }
    }
}
