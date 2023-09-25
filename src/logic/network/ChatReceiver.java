package logic.network;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static logic.network.Exchange.decisionHandler;

public class ChatReceiver {
    String database;
    boolean isConnected;
    ArrayList<History> history;
    User receiver;
    Contact sender;
    ContactManager contactManager;
    ServerSocket receiverSocket;
    Socket senderSocket;
    BufferedReader in;
    PrintWriter out;
    Thread receiverHandshakeListenerThread;
    JFrame frame;

    public ChatReceiver(User user, ContactManager contactManager, String database, JFrame frame) {
        this.database = database;
        isConnected = false;
        history = new ArrayList<>();
        receiver = user;
        sender = null;
        this.contactManager = contactManager;
        receiverSocket = null;
        senderSocket = null;
        in = null;
        out = null;
        this.frame = frame;
        receiverHandshakeListenerThread = new Thread(this::receiverHandshakeListener);
        receiverHandshakeListenerThread.start();
    }

    private void receiverHandshakeListener() {
        try (ServerSocket serverSocket = new ServerSocket(Constant.chatHandshakePort)) {
            Socket clientSocket = null;
            BufferedReader in = null;
            PrintWriter out = null;

            while (!Thread.currentThread().isInterrupted()) {
                if (isConnected) continue;

                clientSocket = serverSocket.accept();
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                // Receive userDetails
                String[] userDetails = in.readLine().split(":");
                String senderId = userDetails[0];
                String senderUserName = userDetails[1];
                String senderIpAddress = userDetails[2];

                // Decision and send signal
                boolean decision = decisionHandler(senderUserName, senderIpAddress, "chat connection", frame);
                if (decision) {
                    out.println(Constant.acceptSignal);
                    isConnected = true;
                    sender = contactManager.getContact(senderId);

                    loadHistory();
                    initConnection();
                } else {
                    out.println(Constant.refuseSignal);
                }
            }

            if (clientSocket != null) clientSocket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadHistory() throws IOException {
        HistoryDataBase.initialization(sender.getId(), database);
        history = HistoryDataBase.getHistoryFromDatabase(sender.getId(), database);
    }

    private void initConnection() throws IOException {
        receiverSocket = new ServerSocket(Constant.chatPort);
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

    public String receive() throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        // Needs to be on it's own thread and in a loop
        String receivedMessage = in.readLine();
        if (receivedMessage == null) return null;

        String[] splitMessage = receivedMessage.split(" // ");
        String senderUserName = splitMessage[0];
        String messageDateTime = splitMessage[1];
        String encryptedMessage = splitMessage[2];

        history.add(new History(senderUserName, messageDateTime, encryptedMessage));

        return Crypto.decryptAES(encryptedMessage, sender.getAESKey(), sender.getIv());
    }

    public void closeSession() throws IOException {
        HistoryDataBase.addIntoDatabase(sender.getId(), history, database);

        isConnected = false;
        if (sender != null) sender = null;
        if (senderSocket != null) senderSocket.close();
        if (senderSocket != null) senderSocket.close();
        if (in != null) in.close();
        if (out != null) out.close();
    }

    public void closeListener() {
        if (receiverHandshakeListenerThread != null) receiverHandshakeListenerThread.interrupt();
    }
}
