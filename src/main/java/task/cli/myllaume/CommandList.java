package task.cli.myllaume;

import java.util.ArrayList;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "list", description = "Lister les tâches")
public class CommandList implements Runnable {
    private final TaskRepository repo;

    public CommandList(TaskRepository repo) {
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
        ArrayList<Task> tasks = repo.read();

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
        ArrayList<Task> tasks = repo.read();

        ArrayList<Task> tasksCompleted = new ArrayList<Task>();
        ArrayList<Task> tasksUncompleted = new ArrayList<Task>();
        for (Task task : tasks) {
            if (!task.getCompleted()) {
                tasksUncompleted.add(task);
            } else {
                tasksCompleted.add(task);
            }
        }

        System.out.println(
                "Done: " + tasksCompleted.size() + " | To do: " + tasksUncompleted.size());
    }

}
