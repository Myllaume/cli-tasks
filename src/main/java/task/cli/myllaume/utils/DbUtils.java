package task.cli.myllaume.utils;

import java.util.Objects;

public class DbUtils {
    public static void throwNullOrNegativeNumber(int number, String message) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(number, message);

        if (number <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
