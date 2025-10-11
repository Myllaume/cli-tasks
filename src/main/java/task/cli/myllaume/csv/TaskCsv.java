package task.cli.myllaume.csv;

public class TaskCsv {
  private final int id;
  private String description;
  private boolean completed;

  public TaskCsv(int id, String description, boolean completed) {
    this.id = id;
    this.description = description;
    this.completed = completed;
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

  @Override
  public String toString() {
    return "[" + (completed ? "✓" : "✗") + "] " + description;
  }

  public String toCsv() {
    return description + "," + (completed ? "true" : "false");
  }
}
