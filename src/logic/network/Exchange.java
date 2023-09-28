package logic.network;

import gui.dialog.RequestDialog;
import logic.data.Constant;
import logic.data.Contact;
import logic.data.Peer;
import logic.data.User;
import logic.manager.ContactManager;
import logic.security.Crypto;
import logic.security.KeyString;
import logic.storage.ContactDataBase;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

public class Exchange {
    public static boolean knowEachOther(User user, Peer peer, ContactManager contactManager, String database) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException, SQLException {
        try (Socket socket = new Socket(peer.ip(), Constant.EXCHANGE_PORT)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Send user details to display who is trying to connect
            String userDetails = user.getId() + ":" + user.getUserName() + ":" + Address.getLocalIp();
            out.println(userDetails);

            // Receive decision signal
            if (in.readLine().equals(Constant.REFUSED)) return false;

            // Receive public key
            PublicKey receiverPublicKey = KeyString.StringToPublicKey(in.readLine());

            // Generate AES key and IV
            String keyAESString = KeyString.SecretKeyToString(Crypto.generateAESKey(Constant.KEY_SIZE_AES_128));
            String ivString = KeyString.IvToString(Crypto.generateIv());

            // Encrypt with receiver's public key
            String encryptedKeyAESString = Crypto.encryptRSA(receiverPublicKey, keyAESString);
            String encryptedIvString = Crypto.encryptRSA(receiverPublicKey, ivString);

            // Send to receiver + own public key
            out.println(encryptedKeyAESString + ":" + encryptedIvString + ":" + KeyString.PublicKeyToString(user.getPublicKey()));

            // Add to contact
            Contact receiver = new Contact(peer.id(), peer.userName(), KeyString.PublicKeyToString(receiverPublicKey), keyAESString, ivString);
            contactManager.addContact(receiver);
            ContactDataBase.addIntoDatabase(receiver, user, database);
        } catch (ConnectException ignored) {
            return false;
        }
        return true;
    }

    public static ContactManager listener(User user, ContactManager contactManager, String database, JFrame frame) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InterruptedException, InvalidKeySpecException, InvalidAlgorithmParameterException, SQLException {
        try (ServerSocket socket = new ServerSocket(Constant.EXCHANGE_PORT)) {
            Socket clientSocket = socket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Receive userDetails
            String[] userDetails = in.readLine().split(":");
            String senderId = userDetails[0];
            String senderUserName = userDetails[1];
            String senderIpAddress = userDetails[2];

            if (senderId.equals(user.getId())) return null;

            // Decision and send signal
            boolean decision = decisionHandler(senderUserName, senderIpAddress, "contact exchange", frame);
            Thread.sleep(500);
            if (decision) {
                out.println(Constant.ACCEPTED);
            } else {
                out.println(Constant.REFUSED);
                return null;
            }

            // Send own public key
            out.println(KeyString.PublicKeyToString(user.getPublicKey()));

            // Receive encrypted AES key and iv
            String[] receivedKeys = in.readLine().split(":");
            String keyAESString = Crypto.decryptRSA(user.getPrivateKey(), receivedKeys[0]);
            String ivString = Crypto.decryptRSA(user.getPrivateKey(), receivedKeys[1]);
            String senderPublicKey = receivedKeys[2];

            // Add sender to contact
            Contact sender = new Contact(senderId, senderUserName, senderIpAddress, senderPublicKey, keyAESString, ivString);
            contactManager.addContact(sender);
            ContactDataBase.addIntoDatabase(sender, user, database);
            return contactManager;
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    static boolean decisionHandler(String senderUserName, String senderIpAddress, String messageInput, JFrame frame) {
        String message = "Incoming " + messageInput + " request from:\n" +
                "Username: " + senderUserName + "\n" +
                "IP Address: " + senderIpAddress + "\n\n" +
                "Do you want to accept?";

        RequestDialog dialog = new RequestDialog(frame, message);
        dialog.display();
        return dialog.getResult();
    }
}
