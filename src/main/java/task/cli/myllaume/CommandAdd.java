package task.cli.myllaume;

import java.time.Instant;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import task.cli.myllaume.db.TaskManager;

@Command(name = "add", description = "Ajouter une tâche")
public class CommandAdd implements Runnable {
  private final TaskManager manager;

  public CommandAdd(TaskManager manager) {
    this.manager = manager;
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
      Task task =
          manager.createTaskOnCurrentProject(
              TaskData.of(
                  description,
                  completed,
                  TaskPriority.fromLevel(priority),
                  Instant.now(),
                  null,
                  null));
      System.out.println("La tâche '" + task.toIdString() + "' a été ajoutée.");
    } catch (Exception e) {
      System.out.println("La tâche n'a pas été ajoutée: " + e.getMessage());
    }
  }
}
