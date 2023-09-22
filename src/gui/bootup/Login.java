package gui.bootup;

import gui.contact.ContactGUI;
import logic.data.User;
import logic.storage.DataBase;
import logic.storage.UserDataBase;

import javax.swing.*;
import java.io.IOException;
import java.util.Objects;

public class Login {
    private JPanel loginPanel;
    private JTextField userNameTextField;
    private JPasswordField passwordField;
    private JLabel logoLabel;
    private JButton enterButton;
    private JLabel title;
    private JLabel userNameLabel;
    private JLabel passwordLabel;
    private JLabel userNameWarning;
    private JLabel passwordWarning;
    private JButton registerButton;


    public Login() {
        registerButton.setBorderPainted(false);

        enterButton.addActionListener(e -> {
            userNameWarning.setText("");
            passwordWarning.setText("");

            boolean correct = true;
            String userName = userNameTextField.getText();
            String password = new String(passwordField.getPassword());

            if (userNameTextField.getText().isEmpty()) {
                userNameWarning.setText("Must not be empty");
                correct = false;
            }
            if (password.isEmpty()) {
                passwordWarning.setText("Must not be empty");
                correct = false;
            }

            if (correct) {
                try {
                    User user = UserDataBase.getUserFromDatabase(userName, password, DataBase.getDataBasePath());
                    if (user != null) {
                        SplashScreen.user = user;
                        SplashScreen.changePanel(ContactGUI.getContact());
                    } else {
                        userNameWarning.setText("User not found");
                        passwordWarning.setText("User not found");
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        registerButton.addActionListener(e -> {
            SplashScreen.changePanel(Register.getRegister());
        });
    }

    public static JPanel getLogin() {
        return new Login().loginPanel;
    }

    private void createUIComponents() {
        ImageIcon logoIcon = new ImageIcon(Objects.requireNonNull(Login.class.getResource("/CyChatLogo.png")));
        logoLabel = new JLabel(logoIcon);
    }
}
