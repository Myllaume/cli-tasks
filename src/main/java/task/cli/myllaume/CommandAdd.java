package task.cli.myllaume;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "add", description = "Ajouter une tâche")
public class CommandAdd implements Runnable {
    private final TaskRepository repo;

    public CommandAdd(TaskRepository repo) {
        this.repo = repo;
    }

    @Parameters(index = "0", description = "Nom de la tâche")
    String description;

    @Option(names = "--completed", description = "Marquer la tâche comme terminée")
    boolean completed;

    @Override
    public void run() {
        repo.addLineAtEnd(new Task(1, description, completed));
        System.out.println("La tâche " + description + " a été ajoutée.");
    }

}
