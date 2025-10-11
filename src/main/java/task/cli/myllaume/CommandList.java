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

    if (tasks.isEmpty()) {
      System.out.println("Aucune tâche trouvée.");
      return;
    }

    System.out.println("Liste des tâches");

    int maxIdWidth =
        tasks.stream().mapToInt(task -> String.valueOf(task.getId()).length()).max().orElse(1);

    tasks.forEach(
        task -> {
          String idStr = String.format("%" + maxIdWidth + "d", task.getId());
          System.out.println(idStr + ". " + task.toString());
        });
  }

  private void count() {

    try {
      int tasksDone = repo.countTasksDone();
      int tasksTodo = repo.countTasksTodo();

      System.out.println("Done: " + tasksDone + " | To do: " + tasksTodo);
    } catch (Exception e) {
      System.out.println("Erreur lors du comptage des tâches : " + e.getMessage());
    }
  }
}
