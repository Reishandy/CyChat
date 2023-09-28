package logic.network;

import logic.data.Constant;
import logic.data.ManagersWrapper;
import logic.data.Peer;
import logic.manager.ContactManager;
import logic.manager.PeerManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;

public class Broadcast {
    public static void broadcast(String id, String userName) throws IOException {
        try (DatagramSocket broadcastSocket = new DatagramSocket()) {
            String broadcastMessage = id + ":" + userName + ":" + Address.getLocalIp();

            DatagramPacket packet = new DatagramPacket(
                    broadcastMessage.getBytes(),
                    broadcastMessage.length(),
                    Inet4Address.getByName(Address.getBroadcastAddress()),
                    Constant.BROADCAST_PORT
            );
            broadcastSocket.send(packet);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    public static ManagersWrapper listenForBroadcast(String ownId, ContactManager contactManager, PeerManager peerManager) throws IOException {
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
            if (id.equals(ownId) && ipAddress.equals(Address.getLocalIp())) return null;

            // Ignore existing peer
            if (peerManager.checkPeerExist(id)) return null;

            if (contactManager.checkContactExist(id)) {
                contactManager.updateIpAddress(id, ipAddress);
                return null;
            }

            Peer newPeer = new Peer(id, userName, ipAddress);
            peerManager.addPeer(newPeer);
        } catch (IOException e) {
            throw new IOException(e);
        }

        return new ManagersWrapper(contactManager, peerManager);
    }
}
