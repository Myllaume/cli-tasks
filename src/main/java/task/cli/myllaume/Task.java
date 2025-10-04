package task.cli.myllaume;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class Task {
    private final int id;
    private String description;
    private boolean completed;
    private String fulltext;
    private TaskPriority priority;
    private Instant createdAt;
    private Instant dueDate;
    private Instant doneAt;
    private ArrayList<Task> subTasks;

    private Task(int id, String description, boolean completed, String fulltext, TaskPriority priority,
            Instant createdAt, Instant dueDate, Instant doneAt, ArrayList<Task> subTasks) {
        this.id = id;
        this.description = description;
        this.completed = completed;
        this.fulltext = fulltext;
        this.priority = priority;
        this.createdAt = createdAt;
        this.dueDate = dueDate;
        this.doneAt = doneAt;
        this.subTasks = subTasks;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public boolean getCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getFulltext() {
        return this.fulltext;
    }

    public void setFulltext(String fulltext) {
        this.fulltext = fulltext;
    }

    public ArrayList<Task> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(ArrayList<Task> subTasks) {
        this.subTasks = subTasks;
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

    public String toIdString() {
        return (Integer.toString(id) + ". " + description);
    }

    public String toCsv() {
        return description + "," + (completed ? "true" : "false");
    }

    static public Task fromSqlResult(ResultSet sqlResult) throws SQLException {
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

        return Task.of(id, description, completed, fulltext, TaskPriority.fromLevel(priority), createdAt, dueDate,
                doneAt, null);
    }

    public Duration getTaskDuration() {
        if (createdAt == null || doneAt == null) {
            return null;
        }
        return Duration.between(createdAt, doneAt);
    }

    private static void validateId(int id) throws IllegalArgumentException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be positive");
        }
    }

    private static void validateDescription(String description) throws IllegalArgumentException {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
    }

    private static void validateFulltext(String fulltext) throws IllegalArgumentException {
        if (fulltext == null || fulltext.trim().isEmpty()) {
            throw new IllegalArgumentException("Fulltext cannot be null or empty");
        }
    }

    private static void validatePriority(TaskPriority priority) throws IllegalArgumentException {
        if (priority == null) {
            throw new IllegalArgumentException("Priority cannot be null");
        }
    }

    private static void validateCreatedAt(Instant createdAt) throws IllegalArgumentException {
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt cannot be null");
        }
    }

    static public Task of(int id, String description, boolean completed, String fulltext, TaskPriority priority,
            Instant createdAt, Instant dueDate, Instant doneAt, ArrayList<Task> subTasks) {
        validateId(id);
        validateDescription(description);
        validateFulltext(fulltext);
        validatePriority(priority);
        validateCreatedAt(createdAt);
        // dueDate can be null
        // doneAt can be null
        // subTasks can be null

        return new Task(id, description, completed, fulltext, priority, createdAt, dueDate, doneAt, subTasks);
    }

}
