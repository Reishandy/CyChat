package logic.data;


/**
 * Config class to store constants used in the program.
 *
 * @author Reishandy (isthisruxury@gmail.com)
 */
public class Config {
    /**
     * Security module's constants
     */
    public static final String ALGORITHM_AES = "AES";
    public static final String ALGORITHM_RSA = "RSA";
    public static final String ALGORITHM_AES_CFB = "AES/CFB/PKCS5Padding";
    public static final String HASH_PASSWORD = "PBKDF2WithHmacSHA512";
    public static final String HASH_MAIN_KEY = "PBKDF2WithHmacSHA256";

    public static final int HASH_MODE_PASSWORD = 1;
    public static final int HASH_MODE_MAIN_KEY = 2;
    public static final int SIZE_AES = 256;
    public static final int SIZE_RSA = 2048;
    public static final int SIZE_SUPPORT = 16;
    public static final int SIZE_ITERATION = 10_000;

    /**
     * Database module's constants
     */
    public static final String DATABASE_TYPE = "jdbc:sqlite:";
    public static final String DATABASE_EXTENSION = ".db";
    public static final String DATABASE_NAME = "CyChat";

    public static final int DATABASE_MODE_USER = 1;
    public static final int DATABASE_MODE_CONTACT = 2;
    public static final int DATABASE_MODE_HISTORY = 3;
}
