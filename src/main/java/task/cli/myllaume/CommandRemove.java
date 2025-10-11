package task.cli.myllaume;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "remove", description = "Retirer une tâche")
public class CommandRemove implements Runnable {
  private final TaskRepositorySqlite repo;

  public CommandRemove(TaskRepositorySqlite repo) {
    this.repo = repo;
  }

  @Parameters(index = "0", description = "ID de la tâche")
  String id;

  @Override
  public void run() {
    this.removeTask();
  }

  private void removeTask() {
    try {
      repo.removeTask(Integer.parseInt(id));
      System.out.println("La tâche " + id + " a été supprimée.");
    } catch (Exception e) {
      System.out.println("Erreur lors de la suppression de la tâche " + id + ".");
    }
  }
}
