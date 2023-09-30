package gui.bootup;

import gui.dialog.Error;
import logic.data.User;
import logic.storage.DataBase;
import logic.storage.UserDataBase;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Objects;

public class Register {
    private JPanel registerPanel;
    private JLabel logoLabel;
    private JLabel title;
    private JTextField userNameTextField;
    private JLabel userNameLabel;
    private JLabel userNameWarning;
    private JPasswordField passwordField;
    private JLabel passwordLabel;
    private JLabel passwordWarning;
    private JButton enterButton;
    private JButton loginButton;
    private JPasswordField reInputPasswordField;
    private JLabel reInputPasswordWarning;

    public Register() {
        loginButton.setBorderPainted(false);

        enterButton.addActionListener(e -> {
            userNameWarning.setText("");
            passwordWarning.setText("");
            reInputPasswordWarning.setText("");

            boolean correct = true;
            String userName = userNameTextField.getText();
            String password = new String(passwordField.getPassword());
            String reInputPassword = new String(reInputPasswordField.getPassword());

            if (userNameTextField.getText().isEmpty()) {
                userNameWarning.setText("Must not be empty");
                correct = false;
            }
            if (password.isEmpty()) {
                passwordWarning.setText("Must not be empty");
                correct = false;
            }
            if (reInputPassword.isEmpty()) {
                reInputPasswordWarning.setText("Must not be empty");
                correct = false;
            }

            if (userNameTextField.getText().length() < 3) {
                userNameWarning.setText("Must be 3 characters or more");
                correct = false;
            }
            if (password.length() < 8) {
                passwordWarning.setText("Must be 8 characters or more");
                correct = false;
            }

            if (!password.equals(reInputPassword)) {
                passwordWarning.setText("Password does not match");
                reInputPasswordWarning.setText("Password does not match");
                correct = false;
            }

            if (correct) {
                try {
                    User newUser = new User(userName, password);
                    UserDataBase.addIntoDatabase(newUser, DataBase.getDataBasePath());
                    SplashScreen.changePanel(Login.getLogin());
                } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException |
                         InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                         BadPaddingException | InvalidKeyException | SQLException ex) {
                    Error dialog = new Error(SplashScreen.frame, ex);
                    dialog.display();
                }
            }
        });

        loginButton.addActionListener(e -> {
            SplashScreen.changePanel(Login.getLogin());
        });
    }

    public static JPanel getRegister() {
        return new Register().registerPanel;
    }

    private void createUIComponents() {
        ImageIcon logoIcon = new ImageIcon(Objects.requireNonNull(Register.class.getResource("/CyChatLogo.png")));
        logoLabel = new JLabel(logoIcon);
    }
}
