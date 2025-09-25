package task.cli.myllaume;

import java.util.ArrayList;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "list", description = "Lister les tâches")
public class CommandList implements Runnable {
    private final TaskRepositorySqlite repo;

    public CommandList(TaskRepositorySqlite repo) {
        this.repo = repo;
    }

    @Option(names = "--count", description = "Affiche chaque tâche sur une seule ligne")
    boolean count;

    @Override
    public void run() {
        if (count) {
            this.count();
        } else {
            this.multiline();
        }
    }

    private void multiline() {
        ArrayList<Task> tasks;

        try {
            tasks = repo.getTasks(10);
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération des tâches : " + e.getMessage());
            return;
        }

        ArrayList<String> lines = new ArrayList<String>();
        lines.add("Liste des tâches");
        for (Task task : tasks) {
            lines.add("- " + task.toString());
        }

        for (String line : lines) {
            System.out.println(line);
        }
    }

    private void count() {

        try {
            int tasksDone = repo.countTasksDone();
            int tasksTodo = repo.countTasksTodo();

            System.out.println(
                    "Done: " + tasksDone + " | To do: " + tasksTodo);
        } catch (Exception e) {
            System.out.println("Erreur lors du comptage des tâches : " + e.getMessage());
        }

    }

}
