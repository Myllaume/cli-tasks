package task.cli.myllaume;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import task.cli.myllaume.utils.Validators;

public class TaskData {
  protected final String description;
  protected final boolean completed;
  protected final TaskPriority priority;
  protected final Instant createdAt;
  protected final Instant dueDate;
  protected final Instant doneAt;

  protected TaskData(
      String description,
      boolean completed,
      TaskPriority priority,
      Instant createdAt,
      Instant dueDate,
      Instant doneAt) {
    this.description = description;
    this.completed = completed;
    this.priority = priority;
    this.createdAt = createdAt;
    this.dueDate = dueDate;
    this.doneAt = doneAt;
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

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getDueDate() {
    return dueDate;
  }

  public Instant getDoneAt() {
    return doneAt;
  }

  @Override
  public String toString() {
    return "[" + (completed ? "✓" : " ") + "] " + description;
  }

  public String toCsv() {
    return description + "," + (completed ? "true" : "false");
  }

  public Duration getTaskDuration() {
    if (createdAt == null || doneAt == null) {
      return null;
    }
    return Duration.between(createdAt, doneAt);
  }

  public static TaskData of(
      String description,
      boolean completed,
      TaskPriority priority,
      Instant createdAt,
      Instant dueDate,
      Instant doneAt) {
    Validators.throwNullOrEmptyString(description, "Description cannot be null or empty");
    Objects.requireNonNull(completed, "Completed cannot be null");
    Objects.requireNonNull(priority, "Priority cannot be null");
    Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
    // dueDate can be null
    // doneAt can be null

    return new TaskData(description, completed, priority, createdAt, dueDate, doneAt);
  }
}
