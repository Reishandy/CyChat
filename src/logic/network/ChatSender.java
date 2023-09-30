package logic.network;

import logic.data.Constant;
import logic.data.Contact;
import logic.data.History;
import logic.data.User;
import logic.security.Crypto;
import logic.storage.HistoryDataBase;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ChatSender {
    private final String database;
    private ArrayList<History> history;
    private final User sender;
    private Contact receiver;
    private Socket senderSocket;
    private BufferedReader in;
    private PrintWriter out;

    public ChatSender(User user, String database) {
        this.database = database;
        history = new ArrayList<>();
        sender = user;
        receiver = null;
        senderSocket = null;
        in = null;
        out = null;
    }

    public ArrayList<History> getHistory() {
        return history;
    }

    public Contact getReceiver() {
        return receiver;
    }

    public boolean connect(Contact contact) throws IOException, SQLException {
        if (contact == null) return false;
        this.receiver = contact;

        if (!senderHandshake()) {
            return false;
        }

        initConnection();

        loadHistory();

        return true;
    }

    private boolean senderHandshake() throws IOException {
        try (Socket socket = new Socket(receiver.getIp(), Constant.CHAT_HANDSHAKE_PORT)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Send user details to display who is trying to connect
            String userDetails = sender.getId() + ":" + sender.getUserName() + ":" + Address.getLocalIp();
            out.println(userDetails);

            // Receive decision signal
            if (in.readLine().equals(Constant.REFUSED)) return false;
        } catch (ConnectException | UnknownHostException ignored) {
            return false;
        }
        return true;
    }

    private void loadHistory() throws SQLException {
        HistoryDataBase.initialization(receiver.getId(), database);
        history = HistoryDataBase.getHistoryFromDatabase(receiver.getId(), database);
    }

    private void initConnection() throws IOException {
        senderSocket = new Socket(receiver.getIp(), Constant.CHAT_PORT);
        in = new BufferedReader(new InputStreamReader(senderSocket.getInputStream()));
        out = new PrintWriter(senderSocket.getOutputStream(), true);
    }

    public void send(String message) throws InvalidAlgorithmParameterException, NoSuchPaddingException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, SQLException {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd - HH:mm");
        String formattedDateTime = currentDateTime.format(formatter);
        String encryptedMessage = Crypto.encryptAES(message, receiver.getAESKey(), receiver.getIv());

        History readyHistory = new History(sender.getUserName(), formattedDateTime, encryptedMessage);
        if (!message.equals(Constant.CLOSE_SIGNAL)) {
            history.add(readyHistory);
            HistoryDataBase.addIntoDatabase(sender.getId(), readyHistory, database);
        }

        String readyMessage = readyHistory.userName() + " // " + readyHistory.dateTime() + " // " + readyHistory.message();
        out.println(readyMessage);
    }

    public History receive() throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, SQLException {
        // Needs to be on it's own thread and in a loop
        String receivedMessage = in.readLine();
        if (receivedMessage == null) return null;

        String[] splitMessage = receivedMessage.split(" // ");
        String senderUserName = splitMessage[0];
        String messageDateTime = splitMessage[1];
        String encryptedMessage = splitMessage[2];

        History readyHistory = new History(senderUserName, messageDateTime, encryptedMessage);
        history.add(readyHistory);
        HistoryDataBase.addIntoDatabase(receiver.getId(), readyHistory, database);

        String decryptedMessage = Crypto.decryptAES(encryptedMessage, receiver.getAESKey(), receiver.getIv());

        return new History(senderUserName, messageDateTime, decryptedMessage);
    }

    public void closeSession() throws IOException {
        //if (receiver != null) receiver = null;
        if (senderSocket != null) senderSocket.close();
    }
}
