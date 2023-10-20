package logic.data;


import logic.security.Encoder;
import logic.security.Generator;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

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

    /**
     * Network module's constants
     */
    public static final String UDP_IDENTIFIER_BROADCAST = "BROADCAST";
    public static final String UDP_IDENTIFIER_EXCHANGE = "EXCHANGE";
    public static final String UDP_IDENTIFIER_CONNECT = "CONNECT";
    public static final String UDP_IDENTIFIER_ACCEPTED = "ACCEPTED";
    public static final String UDP_IDENTIFIER_REJECTED = "REJECTED";
    public static final String CHAT_CLOSE_SIGNAL = "PWOEFBapoafbpofhoajCiicbsnoABF=0IQHNF0=9U3R9UBCAOb0=ihef=0aicA0BFQW" +
            "wfu9aBF[OCJdbg9bJLCBOWbfojAKNXOUJEWVB  UJUBE=0IQEFOJANishfoEJFN AC0iqefhojacnoiqEHFOAknn0ij    DWNXj   wojboqif1" +
            "3r[i3j=0aOJFQ330HV =0EIFHOjn09r 3 [oiefho[CN0 13R0Uhkfqt 098 piqfb0C RQ13HIRFEQ hv wugf wIHwkm 90  ewhfij a" +
            "(I KNOW THIS IS NOT A GOOD IDEA BUT... I'M TOO LAZY TO MAKE A PROPER CLOSE SIGNAL)";

    public static final int PORT_MAIN = 2318;
    public static final int PORT_CHAT = 2319;
    public static final int UDP_BUFFER_SIZE = 1024;
    public static final SecretKey UDP_KEY;
    public static final IvParameterSpec UDP_IV;

    static {
        try {
            UDP_KEY = Generator.generateMainKey(
                    "CyChat", Encoder.decode("/687ClBejc54xNoI4QSLTA=="));
            UDP_IV = Encoder.decodeIV("xlo1fV04JLJtVFp/xJncMw==");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

}
