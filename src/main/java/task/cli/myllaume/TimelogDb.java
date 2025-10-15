package task.cli.myllaume;

import java.time.Instant;
import task.cli.myllaume.utils.Validators;

public class TimelogDb extends TimelogData {
  private final int id;

  private TimelogDb(int id, int taskId, Instant startedAt, Instant stoppedAt) {
    super(taskId, startedAt, stoppedAt);
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public static TimelogDb of(int id, int taskId, Instant startedAt, Instant stoppedAt) {
    Validators.throwNullOrNegativeNumber(id, "ID must be a positive integer");
    Validators.throwNullOrNegativeNumber(taskId, "Task ID must be a positive integer");

    TimelogData data = TimelogData.of(taskId, startedAt, stoppedAt);
    return new TimelogDb(id, data.getTaskId(), data.getStartedAt(), data.getStoppedAt());
  }

  public static TimelogDb fromSqlResult(java.sql.ResultSet sqlResult) throws java.sql.SQLException {
    int id = sqlResult.getInt("id");
    int taskId = sqlResult.getInt("task_id");
    Instant startedAt = Instant.ofEpochSecond(sqlResult.getLong("started_at"));
    Instant stoppedAt = Instant.ofEpochSecond(sqlResult.getLong("stopped_at"));

    return TimelogDb.of(id, taskId, startedAt, stoppedAt);
  }
}
