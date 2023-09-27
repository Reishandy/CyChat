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
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class Exchange {
    public static boolean knowEachOther(User user, Peer peer, ContactManager contactManager, String database) {
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
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public static void listener(User user, ContactManager contactManager, JFrame frame) {
        try (ServerSocket socket = new ServerSocket(Constant.EXCHANGE_PORT)) {
            Socket clientSocket = null;
            BufferedReader in = null;
            PrintWriter out = null;

            while (!Thread.currentThread().isInterrupted()) {
                clientSocket = socket.accept();
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                // Receive userDetails
                String[] userDetails = in.readLine().split(":");
                String senderId = userDetails[0];
                String senderUserName = userDetails[1];
                String senderIpAddress = userDetails[2];

                if (senderId.equals(user.getId())) continue;

                // Decision and send signal
                boolean decision = decisionHandler(senderUserName, senderIpAddress, "contact exchange", frame);
                Thread.sleep(500);
                if (decision) {
                    out.println(Constant.ACCEPTED);
                } else {
                    out.println(Constant.REFUSED);
                    continue;
                }

                // Send own public key
                out.println(KeyString.PublicKeyToString(user.getPublicKey()));

                // Receive encrypted AES key and iv
                String[] receivedKeys = in.readLine().split(":");
                String keyAESString = Crypto.decryptRSA(user.getPrivateKey(), receivedKeys[0]);
                String ivString = Crypto.decryptRSA(user.getPrivateKey(), receivedKeys[1]);
                String senderPublicKey = receivedKeys[2];

                // Add sender to contact
                Contact sender = new Contact(senderId, senderUserName, senderPublicKey, keyAESString, ivString);
                contactManager.addContact(sender);
            }

            if (clientSocket != null) clientSocket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException | InterruptedException e) {
            throw new RuntimeException(e);
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
