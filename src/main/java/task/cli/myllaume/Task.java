package task.cli.myllaume;

public class Task {
    private final int id;
    private String description;
    private boolean completed;
    private String fulltext;

    public Task(int id, String description, boolean completed, String fulltext) {
        this.id = id;
        this.description = description;
        this.completed = completed;
        this.fulltext = fulltext;
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
        return "[" + (completed ? "✓" : "✗") + "] " + description;
    }

    public String toIdString() {
        return (Integer.toString(id) + ". " + description);
    }

    public String toCsv() {
        return description + "," + (completed ? "true" : "false");
    }
}
