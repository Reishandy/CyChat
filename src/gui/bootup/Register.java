package gui.bootup;

import logic.data.Constant;
import logic.data.User;
import logic.storage.DataBase;
import logic.storage.UserDataBase;

import javax.swing.*;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
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
                } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException ex) {
                    throw new RuntimeException(ex);
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
