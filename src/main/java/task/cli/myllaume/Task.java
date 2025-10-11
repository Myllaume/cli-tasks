package task.cli.myllaume;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import task.cli.myllaume.utils.Validators;

public class Task extends TaskData {
  private final int id;
  private final String fulltext;
  private final ArrayList<Task> subTasks;

  private Task(
      int id,
      String description,
      boolean completed,
      String fulltext,
      TaskPriority priority,
      Instant createdAt,
      Instant dueDate,
      Instant doneAt,
      ArrayList<Task> subTasks) {
    super(description, completed, priority, createdAt, dueDate, doneAt);

    this.id = id;
    this.fulltext = fulltext;
    this.subTasks = subTasks;
  }

  public int getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public TaskPriority getPriority() {
    return priority;
  }

  public boolean getCompleted() {
    return completed;
  }

  public String getFulltext() {
    return this.fulltext;
  }

  public ArrayList<Task> getSubTasks() {
    return subTasks;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getDueDate() {
    return dueDate;
  }

  public Instant getDoneAt() {
    return doneAt;
  }

  public String toIdString() {
    return (Integer.toString(id) + ". " + description);
  }

  public static Task fromSqlResult(ResultSet sqlResult) throws SQLException {
    int id = sqlResult.getInt("id");
    String description = sqlResult.getString("name");
    boolean completed = sqlResult.getBoolean("completed");
    String fulltext = sqlResult.getString("fulltext");
    int priority = sqlResult.getInt("priority");
    Instant createdAt = Instant.ofEpochSecond(sqlResult.getLong("created_at"));

    Instant dueDate = null;
    long dueDateSeconds = sqlResult.getLong("due_at");
    if (!sqlResult.wasNull()) {
      dueDate = Instant.ofEpochSecond(dueDateSeconds);
    }

    Instant doneAt = null;
    long doneAtSeconds = sqlResult.getLong("done_at");
    if (!sqlResult.wasNull()) {
      doneAt = Instant.ofEpochSecond(doneAtSeconds);
    }

    return Task.of(
        id,
        description,
        completed,
        fulltext,
        TaskPriority.fromLevel(priority),
        createdAt,
        dueDate,
        doneAt,
        null);
  }

  public static Task of(
      int id,
      String description,
      boolean completed,
      String fulltext,
      TaskPriority priority,
      Instant createdAt,
      Instant dueDate,
      Instant doneAt,
      ArrayList<Task> subTasks) {
    Validators.throwNullOrNegativeNumber(id, "ID must be a positive integer");
    Validators.throwNullOrEmptyString(description, "Description cannot be null or empty");
    Validators.throwNullOrEmptyString(fulltext, "Fulltext cannot be null or empty");
    Objects.requireNonNull(completed, "Completed cannot be null");
    Objects.requireNonNull(priority, "Priority cannot be null");
    Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
    // dueDate can be null
    // doneAt can be null
    // subTasks can be null

    return new Task(
        id, description, completed, fulltext, priority, createdAt, dueDate, doneAt, subTasks);
  }
}
