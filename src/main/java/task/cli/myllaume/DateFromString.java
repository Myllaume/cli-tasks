package task.cli.myllaume;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateFromString {
  public static Instant parse(String input, Instant from) {
    if (input == null || input.trim().isEmpty()) {
      return from;
    }

    String normalized = input.trim().toLowerCase();
    LocalDateTime fromDateTime = LocalDateTime.ofInstant(from, ZoneId.of("UTC"));

    if (normalized.equals("demain")) {
      return fromDateTime.plusDays(1).atZone(ZoneId.of("UTC")).toInstant();
    }

    if (normalized.equals("hier")) {
      return fromDateTime.minusDays(1).atZone(ZoneId.of("UTC")).toInstant();
    }

    if (normalized.equals("midi")) {
      return fromDateTime
          .withHour(12)
          .withMinute(0)
          .withSecond(0)
          .withNano(0)
          .atZone(ZoneId.of("UTC"))
          .toInstant();
    }

    DayOfWeek targetDay = parseDayOfWeek(normalized);
    if (targetDay != null) {
      LocalDateTime result = fromDateTime;
      while (result.getDayOfWeek() != targetDay) {
        result = result.plusDays(1);
      }
      return result.atZone(ZoneId.of("UTC")).toInstant();
    }

    Pattern pattern = Pattern.compile("^(\\d+)\\s+(\\w+)$");
    Matcher matcher = pattern.matcher(normalized);

    if (matcher.matches()) {
      int quantity = Integer.parseInt(matcher.group(1));
      String unit = matcher.group(2);

      return applyDuration(fromDateTime, quantity, unit);
    }

    return from;
  }

  private static DayOfWeek parseDayOfWeek(String input) {
    switch (input) {
      case "lundi":
        return DayOfWeek.MONDAY;
      case "mardi":
        return DayOfWeek.TUESDAY;
      case "mercredi":
        return DayOfWeek.WEDNESDAY;
      case "jeudi":
        return DayOfWeek.THURSDAY;
      case "vendredi":
        return DayOfWeek.FRIDAY;
      case "samedi":
        return DayOfWeek.SATURDAY;
      case "dimanche":
        return DayOfWeek.SUNDAY;
      default:
        return null;
    }
  }

  private static Instant applyDuration(LocalDateTime from, int quantity, String unit) {
    LocalDateTime result = from;

    switch (unit) {
      case "minute":
      case "minutes":
      case "min":
        result = from.plusMinutes(quantity);
        break;

      case "heure":
      case "heures":
      case "h":
        result = from.plusHours(quantity);
        break;

      case "jour":
      case "jours":
        result = from.plusDays(quantity);
        break;

      case "semaine":
      case "semaines":
        result = from.plusWeeks(quantity);
        break;

      case "mois":
        result = from.plusMonths(quantity);
        break;

      case "an":
      case "ans":
      case "année":
      case "années":
        result = from.plusYears(quantity);
        break;

      default:
        return from.atZone(ZoneId.of("UTC")).toInstant();
    }

    return result.atZone(ZoneId.of("UTC")).toInstant();
  }
}
