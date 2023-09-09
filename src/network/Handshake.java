package network;

import data.Constant;
import data.Contact;
import data.Peer;
import data.User;
import manager.ContactManager;
import security.Crypto;
import security.KeyString;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class Handshake {
    // TODO: key exchange mechanism and add to contact for both of the user
    public static boolean knowEachOther(User user, Peer peer, ContactManager contactManager) {
        try (Socket socket = new Socket(peer.ip(), Constant.applicationPort)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Send user details to display who is trying to connect
            String userDetails = user.getUserName() + ":" + Inet4Address.getLocalHost().getHostAddress();
            out.println(userDetails);

            // Receive decision signal
            if (in.readLine().equals(Constant.refuseSignal)) return false;

            // Receive public key
            PublicKey receiverPublicKey = KeyString.StringToPublicKey(in.readLine());

            // Generate AES key and IV
            String keyAESString = KeyString.SecretKeyToString(Crypto.generateAESKey(Constant.keySizeAES128));
            String ivString = KeyString.IvToString(Crypto.generateIv());

            // Encrypt with receiver's public key
            String encryptedKeyAESString = Crypto.encryptRSA(receiverPublicKey, keyAESString);
            String encryptedIvString = Crypto.encryptRSA(receiverPublicKey, ivString);

            // Send to receiver + own public key
            out.println(encryptedKeyAESString + ":" + encryptedIvString + ":" + KeyString.PublicKeyToString(user.getPublicKey()));

            // Add to contact
            Contact receiver = new Contact(peer.userName(), KeyString.PublicKeyToString(receiverPublicKey), keyAESString, ivString);
            contactManager.addContact(receiver);
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public static void listener(User user, ContactManager contactManager) {
        try (ServerSocket socket = new ServerSocket(Constant.applicationPort)) {
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = socket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                // Receive userDetails
                String[] userDetails = in.readLine().split(":");
                String senderUserName = userDetails[0];
                String senderIpAddress = userDetails[1];

                // Decision and send signal
                if (decisionHandler(senderUserName, senderIpAddress)) {
                    out.println(Constant.acceptSignal);
                } else {
                    out.println(Constant.refuseSignal);
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
                Contact sender = new Contact(senderUserName, senderPublicKey, keyAESString, ivString);
                contactManager.addContact(sender);
            }
        } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean decisionHandler(String senderUserName, String senderIpAddress) {
        String message = "Incoming contact exchange request from:\n" +
                "Username: " + senderUserName + "\n" +
                "IP Address: " + senderIpAddress + "\n\n" +
                "Do you want to accept?";

        int option = JOptionPane.showConfirmDialog(null, message, "Exchange Request", JOptionPane.YES_NO_OPTION);
        return option == JOptionPane.YES_OPTION;
    }
}
