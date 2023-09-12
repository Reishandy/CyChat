package logic.data;

public class Constant {
    public static String algorithmAES = "AES";
    public static String algorithmRSA = "RSA";
    public static String algorithmAESCBC = "AES/CBC/PKCS5Padding";
    public static String algorithmSHA512 = "SHA-512";
    public static String acceptSignal = "ACCEPTED";
    public static String refuseSignal = "REFUSED";

    public static int keySizeAES128 = 128;
    public static int keySizeAES256 = 256;
    public static int bit16 = 16;
    public static int keySizeRSA = 2048;
    public static int mainKeyIteration = 1000;
    public static int broadcastPort = 6189;
    public static int handshakePort = 6191;
    public static int chatPort = 6193;
    public static int chatHandshakePort = 6195;
    public static int bufferListenForBroadcast = 1024;
}