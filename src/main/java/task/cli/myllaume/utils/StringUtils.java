package task.cli.myllaume.utils;

import java.text.Normalizer;
import java.util.Objects;

public class StringUtils {
    public static String normalizeString(String input) {
        if (input == null) {
            return null;
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("[^\\p{Alnum}]", "");
        normalized = normalized.toLowerCase();
        return normalized;
    }

    public static void throwNullOrEmptyString(String str, String message) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(str, message);

        if (str.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
}
