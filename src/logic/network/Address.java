package logic.network;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

/**
 * Class for handling address lookup. no change for now, so no doc.
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class Address {
    public static String getBroadcastAddress() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();

            if (isWLANInterface(networkInterface)) {
                List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();

                for (InterfaceAddress interfaceAddress : interfaceAddresses) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();

                    if (broadcast instanceof java.net.Inet4Address) {
                        return broadcast.getHostAddress();
                    }
                }
            }
        }
        return null;
    }

    public static String getLocalIp() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();

            if (isWLANInterface(networkInterface)) {
                List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();

                for (InterfaceAddress interfaceAddress : interfaceAddresses) {
                    InetAddress address = interfaceAddress.getAddress();

                    if (!address.isLoopbackAddress() && address.isSiteLocalAddress() && address instanceof java.net.Inet4Address) {
                        return address.getHostAddress();
                    }
                }
            }
        }

        return null;
    }

    public static String getInterface() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();

            if (isWLANInterface(networkInterface)) {
                return networkInterface.getDisplayName();
            }
        }

        return null;
    }

    private static boolean isWLANInterface(NetworkInterface networkInterface) {
        try {
            return networkInterface.isUp() && networkInterface.supportsMulticast() && networkInterface.getHardwareAddress() != null;
        } catch (Exception e) {
            return false;
        }
    }
}

