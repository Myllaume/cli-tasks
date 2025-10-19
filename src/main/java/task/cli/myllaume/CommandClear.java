package task.cli.myllaume;

import picocli.CommandLine.Command;
import task.cli.myllaume.db.ProjectsRepository;

@Command(name = "done", description = "Marquer une tâche comme terminée")
public class CommandClear implements Runnable {
  private final ProjectsRepository repo;

  public CommandClear(ProjectsRepository repo) {
    this.repo = repo;
  }

  @Override
  public void run() {
    try {
      repo.dropTables();
      repo.initTables();
      repo.insertDefaultProjectIfNoneExists(ProjectsRepository.defaultProjectData);
      System.out.println("Toutes les tâches ont été supprimées.");
    } catch (Exception e) {
      System.out.println("Erreur lors du vidage des tâches : " + e.getMessage());
    }
  }
}
