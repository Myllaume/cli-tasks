package task.cli.myllaume;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Task {
    private final int id;
    private String description;
    private boolean completed;
    private String fulltext;
    private TaskPriority priority;

    public Task(int id, String description, boolean completed, String fulltext, TaskPriority priority) {
        this.id = id;
        this.description = description;
        this.completed = completed;
        this.fulltext = fulltext;
        this.priority = priority;
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
        return new Task(id, description, completed, fulltext, TaskPriority.fromLevel(priority));
    }

}
