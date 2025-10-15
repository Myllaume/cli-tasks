package task.cli.myllaume;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import org.junit.Test;

public class DateFromStringTest {
  private final Instant dDay = Instant.parse("2000-01-01T00:00:00Z");

  @Test
  public void testOneDay() {
    Instant result = DateFromString.parse("1 jour", dDay);
    Instant expected = Instant.parse("2000-01-02T00:00:00Z");
    assertEquals(expected, result);
  }

  @Test
  public void testTwoDay() {
    Instant result = DateFromString.parse("2 jours", dDay);
    Instant expected = Instant.parse("2000-01-03T00:00:00Z");
    assertEquals(expected, result);
  }

  @Test
  public void testOneMonth() {
    Instant result = DateFromString.parse("1 mois", dDay);
    Instant expected = Instant.parse("2000-02-01T00:00:00Z");
    assertEquals(expected, result);
  }

  @Test
  public void testOneHour() {
    Instant result = DateFromString.parse("1 heure", dDay);
    Instant result2 = DateFromString.parse("1 h", dDay);

    Instant expected = Instant.parse("2000-01-01T01:00:00Z");
    assertEquals(expected, result);
    assertEquals(expected, result2);
  }

  @Test
  public void testQuarterHour() {
    Instant result = DateFromString.parse("15 minutes", dDay);
    Instant result2 = DateFromString.parse("15 min", dDay);

    Instant expected = Instant.parse("2000-01-01T00:15:00Z");
    assertEquals(expected, result);
    assertEquals(expected, result2);
  }

  @Test
  public void testTomorrow() {
    Instant result = DateFromString.parse("demain", dDay);
    Instant expected = Instant.parse("2000-01-02T00:00:00Z");
    assertEquals(expected, result);
  }

  @Test
  public void testOneWeek() {
    Instant result = DateFromString.parse("1 semaine", dDay);
    Instant expected = Instant.parse("2000-01-08T00:00:00Z");
    assertEquals(expected, result);
  }

  @Test
  public void testTwoWeek() {
    Instant result = DateFromString.parse("2 semaines", dDay);
    Instant expected = Instant.parse("2000-01-15T00:00:00Z");
    assertEquals(expected, result);
  }

  @Test
  public void testYesterday() {
    Instant result = DateFromString.parse("hier", dDay);
    Instant expected = Instant.parse("1999-12-31T00:00:00Z");
    assertEquals(expected, result);
  }

  @Test
  public void testOneYear() {
    Instant result = DateFromString.parse("1 an", dDay);
    Instant expected = Instant.parse("2001-01-01T00:00:00Z");
    assertEquals(expected, result);
  }

  @Test
  public void testMonday() {
    Instant result = DateFromString.parse("lundi", dDay);
    Instant expected = Instant.parse("2000-01-03T00:00:00Z");
    assertEquals(expected, result);
  }

  @Test
  public void testMiddleDay() {
    Instant result = DateFromString.parse("midi", dDay);
    Instant expected = Instant.parse("2000-01-01T12:00:00Z");
    assertEquals(expected, result);
  }
}
