package task.cli.myllaume.utils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FormDataParser {
  public static Map<String, String> parse(String formData) throws IllegalArgumentException {

    Map<String, String> result = new HashMap<>();

    if (formData == null || formData.isEmpty()) {
      return result;
    }

    String[] pairs = formData.split("&");

    for (String pair : pairs) {
      String[] keyValue = pair.split("=", 2);

      if (keyValue.length != 2 || keyValue[0].isEmpty()) {
        continue;
      }

      String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
      String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);

      if (result.get(key) != null) {
        throw new IllegalArgumentException("Key \"" + key + "\" is duplicated.");
      }

      result.put(key, value);
    }

    return result;
  }
}
