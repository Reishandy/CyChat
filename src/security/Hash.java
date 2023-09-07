package security;

import data.Constant;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Hash {
    private static String combinePasswordUserName(String password, String userName) {
        // I don't know a single shit about this... this is supposed to be an XOR stuff...
        int length = Math.min(password.length(), userName.length());
        StringBuilder combined = new StringBuilder();

        for (int i = 0; i < length; i++) {
            char passwordChar = password.charAt(i);
            char userNameChar = userName.charAt(i);

            char combinedChar = (char) (passwordChar ^ userNameChar);

            combined.append(combinedChar);
        }

        return combined.toString();
    }

    public static String hashPassword(String password, String userName) throws NoSuchAlgorithmException {
        String combined = combinePasswordUserName(password, userName);
        MessageDigest digest = MessageDigest.getInstance(Constant.algorithmSHA512);
        byte[] hashBytes = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    public static boolean checkPassword(String password, String userName, String hashedPassword) throws NoSuchAlgorithmException {
        String enteredHash = hashPassword(password, userName);
        return hashedPassword.equals(enteredHash);
    }
}
