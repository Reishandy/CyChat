package network;

import data.Constant;
import data.Peer;
import manager.ContactManager;
import manager.PeerManager;

import java.io.IOException;
import java.net.*;

public class Broadcast {
    public static void broadcast(String userName) throws UnknownHostException {
        try (DatagramSocket broadcastSocket = new DatagramSocket();) {
            while (!Thread.currentThread().isInterrupted()) {
                String broadcastMessage = userName + ":" + Inet4Address.getLocalHost().getHostAddress();

                DatagramPacket packet = new DatagramPacket(
                        broadcastMessage.getBytes(),
                        broadcastMessage.length(),
                        Inet4Address.getByName("255.255.255.255"),
                        Constant.broadcastPort
                );
                broadcastSocket.send(packet);

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void listenForBroadcast(String ownUserName, ContactManager contactManager, PeerManager peerManager) {
        try (DatagramSocket listenSocket = new DatagramSocket(Constant.broadcastPort)) {
            byte[] buffer = new byte[Constant.bufferListenForBroadcast];

            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                listenSocket.receive(packet);

                String[] receivedMessage = new String(packet.getData(), 0, packet.getLength())
                        .split(":");
                String userName = receivedMessage[0];
                String ipAddress = receivedMessage[1];

                if (userName.equals(ownUserName) && ipAddress.equals(Inet4Address.getLocalHost().getHostAddress())) continue;
                if (contactManager.checkContactExist(userName)) {
                    contactManager.updateIpAddress(userName, ipAddress);
                    continue;
                }

                Peer newPeer = new Peer(userName, ipAddress);
                peerManager.addPeer(newPeer);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
