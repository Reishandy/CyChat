package logic.data;

import java.awt.*;

public class Constant {
    public static final String ALGORITHM_AES = "AES";
    public static final String ALGORITHM_RSA = "RSA";
    public static final String AES_CFB_PKCS_5_PADDING = "AES/CFB/PKCS5Padding";
    public static final String ALGORITHM_SHA_512 = "SHA-512";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String REFUSED = "REFUSED";
    public static final String CLOSE_SIGNAL = "CKOPJNE3CJH RFCHO WH0ECBIH 3YBX8UXBN83EGCBX8IWN ZX2EUB RFKBN3EXU BNH R (I KNOW THIS IS NOT A GOOD PRACTICE)";

    public static final int KEY_SIZE_AES_128 = 128;
    public static final int KEY_SIZE_AES_256 = 256;
    public static final int BIT_16 = 16;
    public static final int KEY_SIZE_RSA = 2048;
    public static final int MAIN_KEY_ITERATION = 1000;
    public static final int BROADCAST_PORT = 2318;
    public static final int EXCHANGE_PORT = 2319;
    public static final int CHAT_PORT = 2320;
    public static final int CHAT_HANDSHAKE_PORT = 2321;
    public static final int BROADCAST_TIMES = 7;

    public static final int BUFFER_LISTEN_FOR_BROADCAST = 1024;
    public static final Font NAME_FONT = new Font("Barlow Black", Font.PLAIN, 24);
    public static final Font DETAILS_FONT = new Font("Barlow Medium", Font.PLAIN, 18);
    public static final Color MAIN_ACCENT_COLOR = Color.decode("#8C52FF");
    public static final Color SECONDARY_ACCENT_COLOR = Color.decode("#545454");
}
