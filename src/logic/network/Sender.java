package logic.network;

import logic.data.Config;

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
     * and connect.
     *
     * @param id              The id for the user
     * @param username        The username for the user
     * @param targetIpAddress The target IP address
     * @throws IOException If there is an error while sending the UDP packet
     * @author Reishandy (isthisruxury@gmail.com)
     * @see Receiver
     * @see Config#UDP_MODE_BROADCAST
     * @see Config#UDP_MODE_EXCHANGE
     * @see Config#UDP_MODE_CONNECT
     * @see Config#UDP_IDENTIFIER_BROADCAST
     * @see Config#UDP_IDENTIFIER_EXCHANGE
     * @see Config#UDP_IDENTIFIER_CONNECT
     * @see Config#PORT_MAIN
     * @see DatagramSocket
     * @see DatagramPacket
     * @see Inet4Address
     * @see Address#getLocalIp() Used to get local IP address
     */
    public static void sendUDP(String id, String username, String targetIpAddress, int mode) throws IOException {
        String identifier = switch (mode) {
            case Config.UDP_MODE_BROADCAST -> Config.UDP_IDENTIFIER_BROADCAST;
            case Config.UDP_MODE_EXCHANGE -> Config.UDP_IDENTIFIER_EXCHANGE;
            case Config.UDP_MODE_CONNECT -> Config.UDP_IDENTIFIER_CONNECT;
            default -> "";
        };

        try (DatagramSocket socket = new DatagramSocket()) {
            String broadcastMessage = identifier +  ":" + targetIpAddress + ":" + id + ":" + username + ":" + Address.getLocalIp();

            DatagramPacket packet = new DatagramPacket(
                    broadcastMessage.getBytes(),
                    broadcastMessage.getBytes().length,
                    Inet4Address.getByName(Address.getBroadcastAddress()),
                    Config.PORT_MAIN
            );

            socket.send(packet);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }
}
