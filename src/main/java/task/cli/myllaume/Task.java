package task.cli.myllaume;

import java.sql.ResultSet;
import java.sql.SQLException;
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
    private ArrayList<Task> subTasks;

    public Task(int id, String description, boolean completed, String fulltext, TaskPriority priority,
            Instant createdAt, Instant dueDate, ArrayList<Task> subTasks) {
        this.id = id;
        this.description = description;
        this.completed = completed;
        this.fulltext = fulltext;
        this.priority = priority;
        this.createdAt = createdAt;
        this.dueDate = dueDate;
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
        Instant dueDate = Instant.ofEpochSecond(sqlResult.getLong("due_at"));
        return new Task(id, description, completed, fulltext, TaskPriority.fromLevel(priority), createdAt, dueDate, null);
    }

}
