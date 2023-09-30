package gui.chat;

import gui.bootup.SplashScreen;
import gui.contact.ContactGUI;
import gui.dialog.DisconnectDialog;
import gui.dialog.Error;
import gui.dialog.RefusedDialog;
import logic.data.Constant;
import logic.data.Contact;
import logic.data.History;
import logic.security.Crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

public class ChatGUI {
    private Contact partner;
    private boolean isSender;
    private Thread messageRecieverThread;
    private JPanel chatPanel;
    private JPanel headerPanel;
    private JPanel footerPanel;
    private JPanel chatBubblePanel;
    private JLabel userNameLabel;
    private JLabel topBarLogo;
    private JButton backButton;
    private JButton sendButton;
    private JTextField inputField;
    private JScrollPane scrollPane;

    public ChatGUI() {
        // Set up some gui stuff
        headerPanel.setBackground(Constant.MAIN_ACCENT_COLOR);
        footerPanel.setBackground(Constant.MAIN_ACCENT_COLOR);
        chatBubblePanel.setBackground(Color.WHITE);
        backButton.setBorderPainted(false);
        sendButton.setBorderPainted(false);

        // Init
        initPartner();

        // Load history
        loadChatHistory();

        // Init message receiver thread
        initMessageReceiverThread();

        // set partner username
        userNameLabel.setText(partner.getUserName());

        sendButton.addActionListener(e -> {
            String message = inputField.getText();
            inputField.setText("");

            if (message == null || message.isEmpty()) {
                return;
            }

            sendMessage(message);
        });

        backButton.addActionListener(e -> {
            DisconnectDialog dialog = new DisconnectDialog(SplashScreen.frame);
            dialog.display();
            boolean result = dialog.getResult();

            if (result) {
                try {
                    if (isSender) {
                        SplashScreen.chatSender.closeSession();
                    } else {
                        SplashScreen.chatReceiver.closeSession();
                    }
                } catch (IOException ex) {
                    Error dialog2 = new Error(SplashScreen.frame, ex);
                    dialog2.display();
                }

                sendMessage(Constant.CLOSE_SIGNAL);
                messageRecieverThread.interrupt();

                // TODO: temp fix
                SplashScreen.chatSender = null;
                SplashScreen.chatReceiver = null;

                SplashScreen.changePanel(ContactGUI.getContact());
            }
        });

        // TODO: fix cannot contact again after coming back
    }

    private void loadChatHistory() {
        ArrayList<History> histories;
        if (isSender) {
            histories = SplashScreen.chatSender.getHistory();
        } else {
            histories = SplashScreen.chatReceiver.getHistory();
        }

        for (History history: histories) {
            History decryptedHistory = history;
            try {
                decryptedHistory = new History(history.userName(), history.dateTime(),
                        Crypto.decryptAES(history.message(), partner.getAESKey(), partner.getIv()));
            } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException |
                     InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                Error dialog = new Error(SplashScreen.frame, e);
                dialog.display();
            }
            if (history.userName().equals(partner.getUserName())) {
                addPartnerBubble(decryptedHistory);
            } else {
                addUserBubble(decryptedHistory);
            }
        }
    }

    private void sendMessage(String message) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd - HH:mm");
        History messageToSend = new History(SplashScreen.user.getUserName(), currentDateTime.format(formatter), message);

        try {
            if (isSender) {
                SplashScreen.chatSender.send(message);
            } else {
                SplashScreen.chatReceiver.send(message);
            }
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | SQLException e) {
            Error dialog = new Error(SplashScreen.frame, e);
            dialog.display();
        }

        addUserBubble(messageToSend);
    }

    private void initMessageReceiverThread() {
        if (isSender) {
            messageRecieverThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        History receivedMessage = SplashScreen.chatSender.receive();
                        if (receivedMessage.message().equals(Constant.CLOSE_SIGNAL)) kickedOut();
                        addPartnerBubble(receivedMessage);
                    } catch (NullPointerException | IOException e) {
                        // Exit because partner exited
                        kickedOut();
                        break;
                    } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                             NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | SQLException e) {
                        Error dialog = new Error(SplashScreen.frame, e);
                        dialog.display();
                    }
                }
            });
        } else {
            messageRecieverThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        History receivedMessage = SplashScreen.chatReceiver.receive();
                        if (receivedMessage.message().equals(Constant.CLOSE_SIGNAL)) kickedOut();
                        addPartnerBubble(receivedMessage);
                    } catch (NullPointerException | IOException e) {
                        // Exit because partner exited
                        kickedOut();
                        break;
                    }catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                             NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | SQLException e) {
                        Error dialog = new Error(SplashScreen.frame, e);
                        dialog.display();
                    }
                }
            });
        }

        messageRecieverThread.start();
    }

    private void initPartner() {
        Contact senderContact = SplashScreen.chatReceiver.getSender();
        Contact receiverContact = SplashScreen.chatSender.getReceiver();

        if (senderContact != null) {
            partner = senderContact;
            isSender = false;
        } else {
            partner = receiverContact;
            isSender = true;
        }
    }

    private void updateScrollBar() {
        SwingUtilities.invokeLater(() -> {
            scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
            scrollPane.revalidate();
            scrollPane.repaint();
        });
    }

    private void addPartnerBubble(History message) {
        JPanel bubble = createChatBubble(false, message);

        chatBubblePanel.add(bubble);
        chatBubblePanel.revalidate();
        chatBubblePanel.repaint();

        updateScrollBar();
    }

    private void addUserBubble(History message) {
        JPanel bubble = createChatBubble(true, message);

        chatBubblePanel.add(bubble);
        chatBubblePanel.revalidate();
        chatBubblePanel.repaint();

        updateScrollBar();
    }

    private JPanel createChatBubble(boolean isUser, History message) {
        // Create username and datetime panel
        JPanel userAndTime = new JPanel();
        userAndTime.setLayout(new BoxLayout(userAndTime, BoxLayout.X_AXIS));
        userAndTime.setBackground(Color.WHITE);

        JLabel userNameLabel = new JLabel(message.userName());
        JLabel dateTimeLabel = new JLabel(message.dateTime());

        // Create message JTextArea
        JTextArea messageArea = new JTextArea(message.message());
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        messageArea.setBackground(Color.WHITE);

        // Create bubble
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBackground(Color.WHITE);

        // Set user or partner styling
        if (isUser) {
            messageArea.setBackground(Constant.MAIN_ACCENT_COLOR);
            userAndTime.add(dateTimeLabel);
            userAndTime.add(Box.createHorizontalGlue());
            userAndTime.add(userNameLabel);
            bubble.setAlignmentX(Component.LEFT_ALIGNMENT);
        } else {
            messageArea.setBackground(Constant.SECONDARY_ACCENT_COLOR);
            userAndTime.add(userNameLabel);
            userAndTime.add(Box.createHorizontalGlue());
            userAndTime.add(dateTimeLabel);
            bubble.setAlignmentX(Component.RIGHT_ALIGNMENT);
        }

        // Add message and user and time to bubble
        bubble.add(userAndTime, 0);
        bubble.add(messageArea, 1);

        // Set additional styling
        messageArea.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
        messageArea.setMaximumSize(new Dimension(250, bubble.getPreferredSize().height));

        userAndTime.setMaximumSize(new Dimension(messageArea.getMaximumSize().width, userAndTime.getPreferredSize().height));

        bubble.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        return bubble;
    }

    private void kickedOut() {
        try {
            if (isSender) {
                SplashScreen.chatSender.closeSession();
            } else {
                SplashScreen.chatReceiver.closeSession();
            }
        } catch (IOException e) {
            Error dialog = new Error(SplashScreen.frame, e);
            dialog.display();
        }

        messageRecieverThread.interrupt();

        // TODO: temp fix
        SplashScreen.chatSender = null;
        SplashScreen.chatReceiver = null;

        new RefusedDialog(SplashScreen.frame, "Partner Disconnected").display();
        SplashScreen.changePanel(ContactGUI.getContact());
    }

    public static JPanel getChatPanel() {
        return new ChatGUI().chatPanel;
    }

    private void createUIComponents() {
        ImageIcon logoIcon = new ImageIcon(Objects.requireNonNull(ContactGUI.class.getResource("/CyChatReversed.png")));
        topBarLogo = new JLabel(logoIcon);

        chatBubblePanel = new JPanel();
        chatBubblePanel.setLayout(new BoxLayout(chatBubblePanel, BoxLayout.Y_AXIS));
    }
}


