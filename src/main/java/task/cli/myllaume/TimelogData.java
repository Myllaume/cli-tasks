package task.cli.myllaume;

import java.time.Instant;
import java.util.Objects;
import task.cli.myllaume.utils.Validators;

public class TimelogData {
  private final int taskId;
  private final Instant startedAt;
  private final Instant stoppedAt;

  protected TimelogData(int taskId, Instant startedAt, Instant stoppedAt) {
    this.taskId = taskId;
    this.startedAt = startedAt;
    this.stoppedAt = stoppedAt;
  }

  public int getTaskId() {
    return taskId;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public Instant getStoppedAt() {
    return stoppedAt;
  }

  public long getDurationSeconds() {
    return stoppedAt.getEpochSecond() - startedAt.getEpochSecond();
  }

  public static TimelogData of(int taskId, Instant startedAt, Instant stoppedAt) {
    Validators.throwNullOrNegativeNumber(taskId, "Task ID must be a positive integer");
    Objects.requireNonNull(startedAt, "Timelog startedAt cannot be null");
    Objects.requireNonNull(stoppedAt, "Timelog stoppedAt cannot be null");

    if (stoppedAt.isBefore(startedAt)) {
      throw new IllegalArgumentException("Timelog stoppedAt must be after startedAt");
    }

    return new TimelogData(taskId, startedAt, stoppedAt);
  }
}
