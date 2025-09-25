package task.cli.myllaume;

import java.text.Normalizer;

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
}
