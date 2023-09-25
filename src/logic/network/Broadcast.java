package logic.network;

import logic.data.Constant;
import logic.data.Peer;
import logic.manager.ContactManager;
import logic.manager.ManagersWrapper;
import logic.manager.PeerManager;

import java.io.IOException;
import java.net.*;

public class Broadcast {
    public static void broadcast(String id, String userName) throws UnknownHostException {
        try (DatagramSocket broadcastSocket = new DatagramSocket()) {
            String broadcastMessage = id + ":" + userName + ":" + Inet4Address.getLocalHost().getHostAddress();

            DatagramPacket packet = new DatagramPacket(
                    broadcastMessage.getBytes(),
                    broadcastMessage.length(),
                    Inet4Address.getByName("255.255.255.255"),
                    Constant.BROADCAST_PORT
            );
            broadcastSocket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ManagersWrapper listenForBroadcast(String ownId, ContactManager contactManager, PeerManager peerManager) {
        try (DatagramSocket listenSocket = new DatagramSocket(Constant.BROADCAST_PORT)) {
            byte[] buffer = new byte[Constant.BUFFER_LISTEN_FOR_BROADCAST];

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            listenSocket.receive(packet);

            String[] receivedMessage = new String(packet.getData(), 0, packet.getLength())
                    .split(":");
            String id = receivedMessage[0];
            String userName = receivedMessage[1];
            String ipAddress = receivedMessage[2];

            // Ignore own broadcast
            if (id.equals(ownId) && ipAddress.equals(Inet4Address.getLocalHost().getHostAddress())) return null;

            if (contactManager.checkContactExist(id)) {
                contactManager.updateIpAddress(id, ipAddress);
                return null;
            }

            Peer newPeer = new Peer(id, userName, ipAddress);
            peerManager.addPeer(newPeer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new ManagersWrapper(contactManager, peerManager);
    }
}
