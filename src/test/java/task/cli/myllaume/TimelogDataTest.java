package task.cli.myllaume;

import static org.junit.Assert.*;

import java.time.Instant;
import org.junit.Test;

public class TimelogDataTest {

  @Test
  public void testCreateValidTimelog() {
    Instant start = Instant.now();
    Instant stop = start.plusSeconds(3600);

    TimelogData timelog = TimelogData.of(1, start, stop);

    assertEquals(1, timelog.getTaskId());
    assertEquals(start, timelog.getStartedAt());
    assertEquals(stop, timelog.getStoppedAt());
    assertEquals(3600, timelog.getDurationSeconds());
  }

  @Test
  public void testGetDurationSeconds() {
    Instant start = Instant.ofEpochSecond(1000);
    Instant stop = Instant.ofEpochSecond(2500);

    TimelogData timelog = TimelogData.of(1, start, stop);

    assertEquals(1500, timelog.getDurationSeconds());
  }

  @Test
  public void testThrowExceptionWhenTaskIdIsZero() {
    Instant start = Instant.now();
    Instant stop = start.plusSeconds(3600);

    try {
      TimelogData.of(0, start, stop);
      fail("Should have thrown IllegalArgumentException for task ID 0");
    } catch (IllegalArgumentException e) {
      assertEquals("Task ID must be a positive integer", e.getMessage());
    }
  }

  @Test
  public void testThrowExceptionWhenTaskIdIsNegative() {
    Instant start = Instant.now();
    Instant stop = start.plusSeconds(3600);

    try {
      TimelogData.of(-1, start, stop);
      fail("Should have thrown IllegalArgumentException for negative task ID");
    } catch (IllegalArgumentException e) {
      assertEquals("Task ID must be a positive integer", e.getMessage());
    }
  }

  @Test
  public void testThrowExceptionWhenStartedAtIsNull() {
    Instant stop = Instant.now();

    try {
      TimelogData.of(1, null, stop);
      fail("Should have thrown NullPointerException for null startedAt");
    } catch (NullPointerException e) {
      assertEquals("Timelog startedAt cannot be null", e.getMessage());
    }
  }

  @Test
  public void testThrowExceptionWhenStoppedAtIsNull() {
    Instant start = Instant.now();

    try {
      TimelogData.of(1, start, null);
      fail("Should have thrown NullPointerException for null stoppedAt");
    } catch (NullPointerException e) {
      assertEquals("Timelog stoppedAt cannot be null", e.getMessage());
    }
  }

  @Test
  public void testThrowExceptionWhenStoppedAtIsBeforeStartedAt() {
    Instant start = Instant.now();
    Instant stop = start.minusSeconds(3600);

    try {
      TimelogData.of(1, start, stop);
      fail("Should have thrown IllegalArgumentException when stoppedAt is before startedAt");
    } catch (IllegalArgumentException e) {
      assertEquals("Timelog stoppedAt must be after startedAt", e.getMessage());
    }
  }

  @Test
  public void testAllowStoppedAtEqualToStartedAt() {
    Instant start = Instant.now();
    Instant stop = start;

    TimelogData timelog = TimelogData.of(1, start, stop);

    assertEquals(0, timelog.getDurationSeconds());
  }
}
