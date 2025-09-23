package task.cli.myllaume;

import java.io.IOException;

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
        try {
            repo.addLineAtEnd(new TaskCsv(1, description, completed));
            System.out.println("La tâche " + description + " a été ajoutée.");
        } catch (IOException e) {
            System.out.println("La tâche " + description + " n'a pas été ajoutée: " + e.getMessage());
        }
    }

}
