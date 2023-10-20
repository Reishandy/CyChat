package logic.network.udp;

import logic.data.Config;
import logic.network.Address;
import logic.security.Crypto;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Receiver class to receive all UDP for this application, broadcast, exchange request, and connect request.
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class Receiver {
    /**
     * Function to receive UDP connection request from other user, this function will receive UDP packet from other user
     * the packet will contain identifier, id, username, and local IP address. and can have 3 mode, broadcast, exchange,
     * and connect. This function's primary purpose is to receive UDP packet from other user and ignore broadcast packet
     * from own IP address, and only receive exchange and connect packet that is sent to this IP address besides that
     * it will be ignored. It will also encrypt the message with a main key.
     *
     * @param ip The local IP address
     * @return String[] The array of string that contains identifier, id, username, and local IP address
     *         or null if the packet is ignored.
     *         [0] = identifier, [1] = sender id, [2] = sender username, [3] = sender IP address
     * @throws Exception If there is an error while receiving the UDP packet
     * @author Reishandy (isthisruxury@gmail.com)
     * @see Sender
     * @see Config#UDP_IDENTIFIER_BROADCAST
     * @see Config#UDP_IDENTIFIER_EXCHANGE
     * @see Config#UDP_IDENTIFIER_CONNECT
     * @see Config#PORT_MAIN
     * @see DatagramSocket
     * @see DatagramPacket
     * @see Address#getLocalIp() Used to get local IP address
     * @see Address#getBroadcastAddress() Used to get broadcast IP address
     * @see Crypto#decryptAES(String, SecretKey, IvParameterSpec)
     * @see Config#UDP_KEY
     * @see Config#UDP_IV
     */
    public static String[] receiveUDP(String ip) throws Exception {
        String[] returnMessage = null;

        try (DatagramSocket socket = new DatagramSocket(Config.PORT_MAIN)) {
            byte[] buffer = new byte[Config.UDP_BUFFER_SIZE];

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            String decryptedMessage = Crypto.decryptAES(
                    new String(packet.getData(), 0, packet.getLength()),
                    Config.UDP_KEY, Config.UDP_IV
            );

            String[] receivedMessage = decryptedMessage.split(":");
            String identifier = receivedMessage[0];
            String targetIpAddress = receivedMessage[1];
            String senderId = receivedMessage[2];
            String senderUsername = receivedMessage[3];
            String senderIpAddress = receivedMessage[4];

            switch (identifier) {
                case Config.UDP_IDENTIFIER_BROADCAST -> {
                    if (!senderIpAddress.equals(ip)) {
                        returnMessage = new String[]{identifier, senderId, senderUsername, senderIpAddress};
                    }
                }
                case Config.UDP_IDENTIFIER_EXCHANGE, Config.UDP_IDENTIFIER_CONNECT -> {
                    if (targetIpAddress.equals(ip)) {
                        returnMessage = new String[]{identifier, senderId, senderUsername, senderIpAddress};
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception(e);
        }

        return returnMessage;
    }
}
