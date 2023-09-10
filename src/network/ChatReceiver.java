package network;

import data.Constant;
import data.Contact;
import data.History;
import data.User;
import manager.ContactManager;
import security.Crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
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

import static network.Exchange.decisionHandler;

public class ChatReceiver { // TODO: write test and database
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

    public ChatReceiver(User user, ContactManager contactManager) {
        isConnected = false;
        history = new ArrayList<>();
        receiver = user;
        sender = null;
        this.contactManager = contactManager;
        receiverSocket = null;
        senderSocket = null;
        in = null;
        out = null;
        receiverHandshakeListenerThread = new Thread(this::receiverHandshakeListener);
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
                String senderUserName = userDetails[0];
                String senderIpAddress = userDetails[1];

                // Decision and send signal
                boolean decision = decisionHandler(senderUserName, senderIpAddress, "chat connection");
                Thread.sleep(500);
                if (decision) {
                    out.println(Constant.acceptSignal);
                    isConnected = true;
                    sender = contactManager.getContact(senderUserName);

                    loadHistory();
                    initConnection();
                } else {
                    out.println(Constant.refuseSignal);
                }
            }

            if (clientSocket != null) clientSocket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadHistory() {
        // TODO: load history from database
    }

    private void initConnection() throws IOException {
        receiverSocket = new ServerSocket(Constant.chatPort);
        senderSocket = receiverSocket.accept();
        in = new BufferedReader(new InputStreamReader(senderSocket.getInputStream()));
        out = new PrintWriter(senderSocket.getOutputStream(), true);
    }

    public void send(String message) throws InvalidAlgorithmParameterException, NoSuchPaddingException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        // Needs to be on it's own thread and in a loop
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd - HH:mm");
        String formattedDateTime = currentDateTime.format(formatter);
        String encryptedMessage = Crypto.encryptAES(message, sender.getAESKey(), sender.getIv());

        History readyMessage = new History(receiver.getUserName(), formattedDateTime, encryptedMessage);
        history.add(readyMessage);

        out.println(readyMessage.userName() + ":" + readyMessage.dateTime() + ":" + readyMessage.message());
    }

    public String receive() throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        // Needs to be on it's own thread and in a loop
        String[] receivedMessage = in.readLine().split(":");
        String senderUserName = receivedMessage[0];
        String messageDateTime = receivedMessage[1];
        String encryptedMessage = receivedMessage[2];

        history.add(new History(senderUserName, messageDateTime, encryptedMessage));

        return Crypto.decryptAES(encryptedMessage, sender.getAESKey(), sender.getIv());
    }

    public void closeSession() throws IOException {
        isConnected = false;
        if (sender != null) sender = null;
        if (senderSocket != null) senderSocket.close();
        if (senderSocket != null) senderSocket.close();
        if (in != null) in.close();
        if (out != null) out.close();

        // TODO: save history all at once to be more efficient?
    }

    public void closeListener() {
        if (receiverHandshakeListenerThread != null) receiverHandshakeListenerThread.interrupt();
    }
}
