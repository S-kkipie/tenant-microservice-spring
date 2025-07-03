package unsa.sistemas.tenantservice.Utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class RandomPasswordGenerator {
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = UPPER.toLowerCase();
    private static final String DIGITS = "0123456789";

    private static final String ALL_CHARS = UPPER + LOWER + DIGITS;

    private static final SecureRandom random = new SecureRandom();

    public static String generateRandomPassword(int length) {
        StringBuilder password = new StringBuilder(length);

        password.append(UPPER.charAt(random.nextInt(UPPER.length())));
        password.append(LOWER.charAt(random.nextInt(LOWER.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));

        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        return URLEncoder.encode(shuffleString(password.toString()), StandardCharsets.UTF_8);
    }

    private static String shuffleString(String input) {
        char[] charArray = input.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            int randomIndex = random.nextInt(charArray.length);
            char temp = charArray[i];
            charArray[i] = charArray[randomIndex];
            charArray[randomIndex] = temp;
        }
        return new String(charArray);
    }
}