package task.cli.myllaume.utils;

import java.util.Objects;

public class Validators {
  public static void throwNullOrNegativeNumber(int number, String message)
      throws NullPointerException, IllegalArgumentException {
    Objects.requireNonNull(number, message);

    if (number <= 0) {
      throw new IllegalArgumentException(message);
    }
  }

  public static void throwNullOrEmptyString(String str, String message)
      throws NullPointerException, IllegalArgumentException {
    Objects.requireNonNull(str, message);

    if (str.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
  }
}
