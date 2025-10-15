package task.cli.myllaume;

import static org.junit.Assert.*;

import java.time.Instant;
import org.junit.Test;

public class TimelogDbTest {

  @Test
  public void testCreateValidTimelogDb() {
    Instant start = Instant.now();
    Instant stop = start.plusSeconds(7200);

    TimelogDb timelog = TimelogDb.of(1, 5, start, stop);

    assertEquals(1, timelog.getId());
    assertEquals(5, timelog.getTaskId());
    assertEquals(start, timelog.getStartedAt());
    assertEquals(stop, timelog.getStoppedAt());
    assertEquals(7200, timelog.getDurationSeconds());
  }

  @Test
  public void testThrowExceptionWhenIdIsZero() {
    Instant start = Instant.now();
    Instant stop = start.plusSeconds(3600);

    try {
      TimelogDb.of(0, 1, start, stop);
      fail("Should have thrown IllegalArgumentException for ID 0");
    } catch (IllegalArgumentException e) {
      assertEquals("ID must be a positive integer", e.getMessage());
    }
  }

  @Test
  public void testThrowExceptionWhenIdIsNegative() {
    Instant start = Instant.now();
    Instant stop = start.plusSeconds(3600);

    try {
      TimelogDb.of(-1, 1, start, stop);
      fail("Should have thrown IllegalArgumentException for negative ID");
    } catch (IllegalArgumentException e) {
      assertEquals("ID must be a positive integer", e.getMessage());
    }
  }

  @Test
  public void testThrowExceptionWhenTaskIdIsInvalid() {
    Instant start = Instant.now();
    Instant stop = start.plusSeconds(3600);

    try {
      TimelogDb.of(1, 0, start, stop);
      fail("Should have thrown IllegalArgumentException for invalid task ID");
    } catch (IllegalArgumentException e) {
      assertEquals("Task ID must be a positive integer", e.getMessage());
    }
  }

  @Test
  public void testInheritanceFromTimelogData() {
    Instant start = Instant.now();
    Instant stop = start.plusSeconds(5400);

    TimelogDb timelog = TimelogDb.of(1, 3, start, stop);

    assertTrue(timelog instanceof TimelogData);
    assertEquals(5400, timelog.getDurationSeconds());
  }

  @Test
  public void testValidateDataViaTimelogDb() {
    Instant start = Instant.now();
    Instant stop = start.minusSeconds(100);

    try {
      TimelogDb.of(1, 1, start, stop);
      fail("Should have thrown IllegalArgumentException when stoppedAt is before startedAt");
    } catch (IllegalArgumentException e) {
      assertEquals("Timelog stoppedAt must be after startedAt", e.getMessage());
    }
  }
}
