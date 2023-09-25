package logic.network;

import logic.data.Constant;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;

public class PortAssigner {
    public static int assignRandomPort() {
        Random random = new Random();
        while (true) {
            int portNumber = random.nextInt(Constant.MAX_PORT_NUMBER - Constant.MIN_PORT_NUMBER + 1) + Constant.MIN_PORT_NUMBER;
            if (isPortAvailable(portNumber)) {
                return portNumber;
            }
        }
    }

    private static boolean isPortAvailable(int portNumber) {
        try (ServerSocket ignored = new ServerSocket(portNumber)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
