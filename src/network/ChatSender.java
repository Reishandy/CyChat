package network;

import data.Constant;
import data.Contact;
import data.History;
import data.User;
import security.Crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.*;

public class ChatSender { // TODO: write test and database
    ArrayList<History> history;
    User sender;
    Contact receiver;
    Socket senderSocket;
    BufferedReader in;
    PrintWriter out;

    public ChatSender(User user) {
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
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(() -> {
            try (Socket socket = new Socket(receiver.getIp(), Constant.chatHandshakePort)) {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Send user details to display who is trying to connect
                String userDetails = sender.getUserName() + ":" + Inet4Address.getLocalHost().getHostAddress();
                out.println(userDetails);

                // Receive decision signal
                if (in.readLine().equals(Constant.refuseSignal)) return false;
            } catch (UnknownHostException e) {
                return false;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        });

        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            return false;
        } finally {
            executor.shutdown();
        }
    }

    private void loadHistory() {
        // TODO: load history from database
        // TODO: dont forget datetime
    }

    private void initConnection() throws IOException {
        senderSocket = new Socket(receiver.getIp(), Constant.chatPort);
        in = new BufferedReader(new InputStreamReader(senderSocket.getInputStream()));
        out = new PrintWriter(senderSocket.getOutputStream(), true);
    }

    public void send(String message) throws InvalidAlgorithmParameterException, NoSuchPaddingException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        // Needs to be on it's own thread and in a loop
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd - HH:mm");
        String formattedDateTime = currentDateTime.format(formatter);
        String encryptedMessage = Crypto.encryptAES(message, receiver.getAESKey(), receiver.getIv());

        History readyMessage = new History(sender.getUserName(), formattedDateTime, encryptedMessage);
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

        return Crypto.decryptAES(encryptedMessage, receiver.getAESKey(), receiver.getIv());
    }

    public void closeSession() throws IOException {
        if (receiver != null) receiver = null;
        if (senderSocket != null) senderSocket.close();
        if (in != null) in.close();
        if (out != null) out.close();

        // TODO: save history all at once to be more efficient?
        // TODO: dont forget date time
    }
}
