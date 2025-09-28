package task.cli.myllaume;

import java.util.ArrayList;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "done", description = "Marquer une tâche comme terminée")
public class CommandDone implements Runnable {
    private final TaskRepositorySqlite repo;

    public CommandDone(TaskRepositorySqlite repo) {
        this.repo = repo;
    }

    @Parameters(index = "0", description = "Id de la tâche")
    int id;

    @Option(names = "--last", description = "Marquer la dernière tâche ajoutée comme terminée")
    boolean completeLastAdded;

    @Override
    public void run() {
        ArrayList<Task> tasksToComplete = new ArrayList<>();

        try {
            if (completeLastAdded) {
                Task lastTask = repo.getLastTask();
                if (lastTask != null) {
                    tasksToComplete.add(lastTask);
                }
            } else {
                Task task = repo.getTask(id);
                if (task != null) {
                    tasksToComplete.add(task);
                }
            }

            if (tasksToComplete.isEmpty()) {
                System.out.println("Aucune tâche trouvée à marquer comme terminée.");
                return;
            }

            for (Task task : tasksToComplete) {
                if (!task.getCompleted()) {
                    repo.updateTask(task.getId(), null, true, null);
                    System.out.println("Tâche '" + task.toIdString() + "' marquée comme terminée.");
                } else {
                    System.out.println("Tâche '" + task.toIdString() + "' est déjà terminée.");
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la mise à jour de la tâche : " + e.getMessage());
        }
    }

}
