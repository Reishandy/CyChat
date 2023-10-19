package logic.network.udp;

import logic.data.Config;
import logic.network.Address;
import logic.security.Crypto;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;

/**
 * Sender class to send all UDP for this application, broadcast, exchange request, and connect request.
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class Sender {
    /**
     * Function to send UDP connection request to target IP address, this function will send UDP packet to target IP
     * the packet will contain identifier, id, username, and local IP address. and can have 3 mode, broadcast, exchange,
     * and connect. It will also encrypt the message with a main key.
     *
     * @param id              The id for the user
     * @param username        The username for the user
     * @param targetIpAddress The target IP address
     * @param identifier      The identifier for the UDP packet
     * @throws Exception If there is an error while sending the UDP packet
     * @author Reishandy (isthisruxury@gmail.com)
     * @see Receiver
     * @see Config#UDP_IDENTIFIER_BROADCAST
     * @see Config#UDP_IDENTIFIER_EXCHANGE
     * @see Config#UDP_IDENTIFIER_CONNECT
     * @see Config#UDP_IDENTIFIER_ACCEPTED
     * @see Config#UDP_IDENTIFIER_REJECTED
     * @see Config#PORT_MAIN
     * @see DatagramSocket
     * @see DatagramPacket
     * @see Inet4Address
     * @see Address#getLocalIp() Used to get local IP address
     * @see Crypto#encryptAES(String, SecretKey, IvParameterSpec)
     * @see Config#UDP_KEY
     * @see Config#UDP_IV
     */
    public static void sendUDP(String id, String username, String targetIpAddress, String identifier) throws Exception {
        try (DatagramSocket socket = new DatagramSocket()) {
            String broadcastMessage = identifier + ":" + targetIpAddress + ":" + id + ":" + username + ":" + Address.getLocalIp();
            String encryptedMessage = Crypto.encryptAES(broadcastMessage, Config.UDP_KEY, Config.UDP_IV);

            DatagramPacket packet = new DatagramPacket(
                    encryptedMessage.getBytes(),
                    encryptedMessage.getBytes().length,
                    Inet4Address.getByName(Address.getBroadcastAddress()),
                    Config.PORT_MAIN
            );

            socket.send(packet);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }
}
