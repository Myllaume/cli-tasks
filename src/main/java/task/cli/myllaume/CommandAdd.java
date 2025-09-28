package task.cli.myllaume;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "add", description = "Ajouter une tâche")
public class CommandAdd implements Runnable {
    private final TaskRepositorySqlite repo;

    public CommandAdd(TaskRepositorySqlite repo) {
        this.repo = repo;
    }

    @Parameters(index = "0", description = "Nom de la tâche")
    String description;

    @Option(names = "--completed", description = "Marquer la tâche créée comme terminée")
    boolean completed;

    @Option(names = "--priority", description = "Priorité de la tâche (1-5)", defaultValue = "1")
    int priority;

    @Override
    public void run() {
        try {
            Task task = repo.createTask(description, completed, TaskPriority.fromLevel(priority));
            System.out.println("La tâche '" + task.toIdString() + "' a été ajoutée.");
        } catch (Exception e) {
            System.out.println("La tâche n'a pas été ajoutée: " + e.getMessage());
        }
    }

}
