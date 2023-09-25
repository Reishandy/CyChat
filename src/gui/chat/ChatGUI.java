package gui.chat;

import javax.swing.*;

public class ChatGUI {
    private JPanel chatPanel;

    public ChatGUI() {

    }

    public static JPanel getChatPanel() {
        return new ChatGUI().chatPanel;
    }
}


