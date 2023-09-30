package logic.network;

import gui.dialog.Error;
import logic.data.Constant;
import logic.data.Contact;
import logic.data.History;
import logic.data.User;
import logic.manager.ContactManager;
import logic.security.Crypto;
import logic.storage.HistoryDataBase;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static logic.network.Exchange.decisionHandler;

public class ChatReceiver {
    private final String database;
    private  ArrayList<History> history;
    private final User receiver;
    private Contact sender;
    private final ContactManager contactManager;
    private ServerSocket receiverSocket;
    private Socket senderSocket;
    private BufferedReader in;
    private PrintWriter out;
    private final JFrame frame;

    public ChatReceiver(User user, ContactManager contactManager, String database, JFrame frame) {
        this.database = database;
        history = new ArrayList<>();
        receiver = user;
        sender = null;
        this.contactManager = contactManager;
        receiverSocket = null;
        senderSocket = null;
        in = null;
        out = null;
        this.frame = frame;
    }

    public ArrayList<History> getHistory() {
        return history;
    }

    public Contact getSender() {
        return sender;
    }

    public boolean receiverHandshakeListener(){
        boolean accepted = false;
        try (ServerSocket serverSocket = new ServerSocket(Constant.CHAT_HANDSHAKE_PORT)) {
            Socket clientSocket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Receive userDetails
            String[] userDetails = in.readLine().split(":");
            String senderId = userDetails[0];
            String senderUserName = userDetails[1];
            String senderIpAddress = userDetails[2];

            // Decision and send signal
            boolean decision = decisionHandler(senderUserName, senderIpAddress, "chat connection", frame);
            if (decision) {
                out.println(Constant.ACCEPTED);
                sender = contactManager.getContact(senderId);

                initConnection();
                loadHistory();

                accepted = true;
            } else {
                out.println(Constant.REFUSED);
            }
        } catch (IOException | SQLException e) {
            Error dialog = new Error(new JFrame(), e);
            dialog.display();
        }

        return accepted;
    }

    private void loadHistory() throws IOException, SQLException {
        HistoryDataBase.initialization(sender.getId(), database);
        history = HistoryDataBase.getHistoryFromDatabase(sender.getId(), database);
    }

    private void initConnection() throws IOException {
        receiverSocket = new ServerSocket(Constant.CHAT_PORT);
        senderSocket = receiverSocket.accept();
        in = new BufferedReader(new InputStreamReader(senderSocket.getInputStream()));
        out = new PrintWriter(senderSocket.getOutputStream(), true);
    }

    public void send(String message) throws InvalidAlgorithmParameterException, NoSuchPaddingException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd - HH:mm");
        String formattedDateTime = currentDateTime.format(formatter);
        String encryptedMessage = Crypto.encryptAES(message, sender.getAESKey(), sender.getIv());

        History readyHistory = new History(receiver.getUserName(), formattedDateTime, encryptedMessage);
        history.add(readyHistory);

        String readyMessage = readyHistory.userName() + " // " + readyHistory.dateTime() + " // " + readyHistory.message();
        out.println(readyMessage);
    }

    public History receive() throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        // Needs to be on it's own thread and in a loop
        String receivedMessage = in.readLine();
        if (receivedMessage == null) return null;

        String[] splitMessage = receivedMessage.split(" // ");
        String senderUserName = splitMessage[0];
        String messageDateTime = splitMessage[1];
        String encryptedMessage = splitMessage[2];

        history.add(new History(senderUserName, messageDateTime, encryptedMessage));

        String decryptedMessage = Crypto.decryptAES(encryptedMessage, sender.getAESKey(), sender.getIv());

        return new History(senderUserName, messageDateTime, decryptedMessage);
    }

    public void saveChat() throws SQLException {
        HistoryDataBase.addIntoDatabase(sender.getId(), history, database);
    }

    public void closeSession() throws IOException {
        //if (sender != null) sender = null;
        if (senderSocket != null) senderSocket.close();
    }
}
