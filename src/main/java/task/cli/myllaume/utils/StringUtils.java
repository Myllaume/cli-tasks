package task.cli.myllaume.utils;

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

  public static String escapeHtml(String input) {
    if (input == null) {
      return null;
    }
    StringBuilder escaped = new StringBuilder();
    for (char c : input.toCharArray()) {
      switch (c) {
        case '<':
          escaped.append("&lt;");
          break;
        case '>':
          escaped.append("&gt;");
          break;
        case '&':
          escaped.append("&amp;");
          break;
        case '"':
          escaped.append("&quot;");
          break;
        case '\'':
          escaped.append("&#x27;");
          break;
        case '/':
          escaped.append("&#x2F;");
          break;
        case '`':
          escaped.append("&#x60;");
          break;
        default:
          escaped.append(c);
      }
    }
    return escaped.toString();
  }
}
