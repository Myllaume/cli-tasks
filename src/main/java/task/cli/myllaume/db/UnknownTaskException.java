package task.cli.myllaume.db;

public class UnknownTaskException extends IllegalArgumentException {
    private final int taskId;

    public UnknownTaskException(int taskId) {
        super("Aucune tâche trouvée avec l'ID: " + taskId);
        this.taskId = taskId;
    }

    public int getTaskId() {
        return taskId;
    }
}
