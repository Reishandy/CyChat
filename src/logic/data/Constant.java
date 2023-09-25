package logic.data;

import java.awt.*;

public class Constant {
    public static final String ALGORITHM_AES = "AES";
    public static final String ALGORITHM_RSA = "RSA";
    public static final String AES_CFB_PKCS_5_PADDING = "AES/CFB/PKCS5Padding";
    public static final String ALGORITHM_SHA_512 = "SHA-512";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String REFUSED = "REFUSED";

    public static final int KEY_SIZE_AES_128 = 128;
    public static final int KEY_SIZE_AES_256 = 256;
    public static final int BIT_16 = 16;
    public static final int KEY_SIZE_RSA = 2048;
    public static final int MAIN_KEY_ITERATION = 1000;
    public static final int MIN_PORT_NUMBER = 1024;
    public static final int MAX_PORT_NUMBER = 49151;
    public static final int BUFFER_LISTEN_FOR_BROADCAST = 1024;
    public static final Font NAME_FONT = new Font("Barlow Black", Font.PLAIN, 24);
    public static final Font DETAILS_FONT = new Font("Barlow Medium", Font.PLAIN, 18);
    public static final Color MAIN_ACCENT_COLOR = Color.decode("#8C52FF");
    public static final Color SECONDARY_ACCENT_COLOR = Color.decode("#545454");
}
