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
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

    public boolean connect(Contact contact) throws IOException {
        if (contact == null) return false;
        this.receiver = contact;

        if (!senderHandshake()) return false;

        loadHistory();

        initConnection();

        return true;
    }

    private boolean senderHandshake() {
        try (Socket socket = new Socket(receiver.getIp(), Constant.CHAT_HANDSHAKE_PORT)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Send user details to display who is trying to connect
            String userDetails = sender.getId() + ":" + sender.getUserName() + ":" + Inet4Address.getLocalHost().getHostAddress();
            out.println(userDetails);

            // Receive decision signal
            if (in.readLine().equals(Constant.REFUSED)) return false;
        } catch (ConnectException | UnknownHostException ignored) {
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private void loadHistory() {
        HistoryDataBase.initialization(receiver.getId(), database);
        history = HistoryDataBase.getHistoryFromDatabase(receiver.getId(), database);
    }

    private void initConnection() throws IOException {
        senderSocket = new Socket(receiver.getIp(), Constant.CHAT_PORT);
        in = new BufferedReader(new InputStreamReader(senderSocket.getInputStream()));
        out = new PrintWriter(senderSocket.getOutputStream(), true);
    }

    public void send(String message) throws InvalidAlgorithmParameterException, NoSuchPaddingException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd - HH:mm");
        String formattedDateTime = currentDateTime.format(formatter);
        String encryptedMessage = Crypto.encryptAES(message, receiver.getAESKey(), receiver.getIv());

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

        return Crypto.decryptAES(encryptedMessage, receiver.getAESKey(), receiver.getIv());
    }

    public void closeSession() throws IOException {
        HistoryDataBase.addIntoDatabase(receiver.getId(), history, database);

        if (receiver != null) receiver = null;
        if (senderSocket != null) senderSocket.close();
        if (in != null) in.close();
        if (out != null) out.close();
    }
}
