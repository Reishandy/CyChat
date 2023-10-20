package logic.network;

import logic.data.Config;
import logic.data.user.Contact;
import logic.data.user.History;
import logic.data.user.User;
import logic.security.Crypto;
import logic.security.Encoder;
import logic.security.Generator;
import logic.storage.DatabaseHistory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Chat class to handle all chat related function. This class will handle all chat related function, such as sending
 * chat, receiving chat, and storing chat history.
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class Chat {
    private final String databasePath;
    private ArrayList<History> history;
    private final User user;
    private Contact partner;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    /**
     * Constructor for Chat class, this constructor will initialize all variable needed for Chat class.
     *
     * @param user         The user that will use this chat
     * @param databasePath The database path for the user
     * @author Reishandy (isthisruxury@gmail.com)
     * @see User
     * @see Contact
     * @see Socket
     * @see BufferedReader
     * @see PrintWriter
     * @see ArrayList
     * @see DatabaseHistory
     * @see NetworkInterface
     * @see Generator#generateIV()
     * @see Encoder#encode(byte[])
     * @see Crypto#encryptAES(String, javax.crypto.SecretKey, IvParameterSpec)
     * @see LocalDateTime
     * @see DateTimeFormatter
     * @see History
     * @see DatabaseHistory#getHistoryFromDatabase(String, String)
     * @see DatabaseHistory#addHistoryIntoDatabase(String, History, String)
     */
    public Chat(User user, String databasePath) {
        this.databasePath = databasePath;
        this.history = new ArrayList<>();
        this.user = user;
        this.partner = null;
        this.socket = null;
        this.input = null;
        this.output = null;
    }

    /**
     * Function to connect to partner, this function will connect to partner and initialize all input and output
     * stream. It will also load chat history from database.
     *
     * @param partner The partner that will be connected
     * @throws IOException  If there is an error while connecting to partner
     * @throws SQLException If there is an error while loading chat history from database
     * @author Reishandy (isthisruxury@gmail.com)
     * @see Contact
     * @see Socket
     * @see BufferedReader
     * @see PrintWriter
     */
    public void connect(Contact partner) throws IOException, SQLException {
        this.partner = partner;
        socket = new Socket(partner.getIpAddress(), Config.PORT_CHAT);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);

        loadHistory();
    }

    /**
     * Function to connect as server, this function will connect as server and initialize all input and output
     * stream. It will also load chat history from database.
     *
     * @param partner The partner that will be connected
     * @throws IOException  If there is an error while connecting to partner
     * @throws SQLException If there is an error while loading chat history from database
     * @author Reishandy (isthisruxury@gmail.com)
     * @see Contact
     * @see Socket
     * @see BufferedReader
     * @see PrintWriter
     */
    public void connectAsServer(Contact partner) throws IOException, SQLException {
        this.partner = partner;
        try (ServerSocket serverSocket = new ServerSocket(Config.PORT_CHAT)) {
            socket = serverSocket.accept();
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new IOException(e);
        }

        loadHistory();
    }

    /**
     * Function to load chat history from database.
     *
     * @throws SQLException If there is an error while loading chat history from database
     * @author Reishandy (isthisruxury@gmail.com)
     * @see DatabaseHistory#getHistoryFromDatabase(String, String)
     * @see History
     */
    private void loadHistory() throws SQLException {
        history = DatabaseHistory.getHistoryFromDatabase(partner.getUserId(), databasePath);
    }

    /**
     * Function to send message to partner, this function will send message to partner and store the message to
     * chat history. It will also encrypt the message with partner's chat key. If the message is a close signal,
     * it will not store the message to chat history. It will also send the message to partner. The message will
     * be formatted as: userId-|CYCHAT|-username-|CYCHAT|-dateTime-|CYCHAT|-encryptedMessage-|CYCHAT|-encodedIV.
     *
     * @param message The message that will be sent to partner
     * @throws NoSuchAlgorithmException           If there is an error while encrypting the message
     * @throws InvalidAlgorithmParameterException If there is an error while encrypting the message
     * @throws NoSuchPaddingException             If there is an error while encrypting the message
     * @throws IllegalBlockSizeException          If there is an error while encrypting the message
     * @throws BadPaddingException                If there is an error while encrypting the message
     * @throws InvalidKeyException                If there is an error while encrypting the message
     * @throws SQLException                       If there is an error while storing the message to chat history
     * @author Reishandy (isthisruxury@gmail.com)
     * @see LocalDateTime
     * @see DateTimeFormatter
     * @see Generator#generateIV()
     * @see Encoder#encode(byte[])
     * @see Crypto#encryptAES(String, javax.crypto.SecretKey, IvParameterSpec)
     * @see History
     * @see DatabaseHistory#addHistoryIntoDatabase(String, History, String)
     * @see Config#CHAT_CLOSE_SIGNAL
     */
    public void send(String message) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, SQLException {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd - HH:mm");
        String formattedDateTime = currentDateTime.format(formatter);

        IvParameterSpec iv = Generator.generateIV();
        String encodedIV = Encoder.encode(iv.getIV());
        String encryptedMessage = Crypto.encryptAES(message, partner.getKeyChatAES(), iv);

        if (!message.equals(Config.CHAT_CLOSE_SIGNAL)) {
            History History = new History(user.getUserId(), user.getUsername(), formattedDateTime, encryptedMessage, encodedIV);
            history.add(History);
            DatabaseHistory.addHistoryIntoDatabase(partner.getUserId(), History, databasePath);
        }

        String messageReady = user.getUserId() + "-|CYCHAT|-" + user.getUsername() + "-|CYCHAT|-" + formattedDateTime +
                "-|CYCHAT|-" + encryptedMessage + "-|CYCHAT|-" + encodedIV;
        output.println(messageReady);
    }

    /**
     * Function to receive message from partner, this function will receive message from partner and store the message
     * to chat history. It will also decrypt the message with partner's chat key. If the message is a close signal,
     * it will not store the message to chat history. It will also return the message. The message will be formatted
     * as: userId-|CYCHAT|-username-|CYCHAT|-dateTime-|CYCHAT|-encryptedMessage-|CYCHAT|-encodedIV.
     *
     * @return The message that is received from partner, null if the partner is disconnected or the message is null
     * format [username, dateTime, message]
     * @throws NoSuchAlgorithmException           If there is an error while encrypting the message
     * @throws InvalidAlgorithmParameterException If there is an error while encrypting the message
     * @throws NoSuchPaddingException             If there is an error while encrypting the message
     * @throws IllegalBlockSizeException          If there is an error while encrypting the message
     * @throws BadPaddingException                If there is an error while encrypting the message
     * @throws InvalidKeyException                If there is an error while encrypting the message
     * @throws SQLException                       If there is an error while storing the message to chat history
     * @author Reishandy (isthisruxury@gmail.com)
     * @see LocalDateTime
     * @see DateTimeFormatter
     * @see Generator#generateIV()
     * @see Encoder#encode(byte[])
     * @see Crypto#decryptAES(String, SecretKey, IvParameterSpec)
     * @see History
     * @see DatabaseHistory#addHistoryIntoDatabase(String, History, String)
     * @see Config#CHAT_CLOSE_SIGNAL
     */
    public String[] receive() throws IOException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, SQLException {
        String message = input.readLine();
        if (message == null) return null;

        String[] messageSplit = message.split("-\\|CYCHAT\\|-");
        String userId = messageSplit[0];
        String username = messageSplit[1];
        String dateTime = messageSplit[2];
        String encryptedMessage = messageSplit[3];
        String encodedIV = messageSplit[4];
        String decryptedMessage = Crypto.decryptAES(encryptedMessage, partner.getKeyChatAES(), Encoder.decodeIV(encodedIV));

        if (!decryptedMessage.equals(Config.CHAT_CLOSE_SIGNAL)) {
            History History = new History(userId, username, dateTime, encryptedMessage, encodedIV);
            history.add(History);
            DatabaseHistory.addHistoryIntoDatabase(partner.getUserId(), History, databasePath);
        }

        return new String[]{username, dateTime, decryptedMessage};
    }

    /**
     * Function to close the chat, this function will close the socket, input, and output stream.
     *
     * @throws IOException If there is an error while closing the socket, input, and output stream
     * @author Reishandy (isthisruxury@gmail.com)
     */
    public void close() throws IOException {
        partner = null;
        if (socket != null) socket.close();
        if (input != null) input.close();
        if (output != null) output.close();
    }

    public ArrayList<History> getHistory() {
        return history;
    }

    public Contact getPartner() {
        return partner;
    }
}
