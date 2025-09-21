package task.cli.myllaume;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "done", description = "Marquer une tâche comme terminée")
public class CommandDone implements Runnable {
    private final TaskRepository repo;

    public CommandDone(TaskRepository repo) {
        this.repo = repo;
    }

    @Parameters(index = "0", description = "Id de la tâche")
    int id;

    @Option(names = "--last", description = "Marquer la dernière tâche ajoutée comme terminée")
    boolean completeLastAdded;

    @Option(names = "--fulltext", description = "Marquer toutes les tâches contenant le texte comme terminées")
    boolean completeFullTextSearch;

    @Override
    public void run() {
        System.out.println("WIP.");
    }

}
