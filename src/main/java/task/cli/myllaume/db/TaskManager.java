package task.cli.myllaume.db;

import task.cli.myllaume.ProjectDb;
import task.cli.myllaume.Task;
import task.cli.myllaume.TaskData;
import task.cli.myllaume.TaskRepositorySqlite;

public class TaskManager {
  private TaskRepositorySqlite taskRepo;
  private ProjectsRepository projectsRepo;

  public TaskManager(TaskRepositorySqlite taskRepo, ProjectsRepository projectsRepo)
      throws Exception {
    this.taskRepo = taskRepo;
    this.projectsRepo = projectsRepo;

    taskRepo.initTables();
  }

  public Task createTaskOnCurrentProject(TaskData data) throws Exception {
    ProjectDb currentProject = projectsRepo.getCurrentProject();
    if (currentProject == null) {
      throw new Exception("No current project set.");
    }

    return taskRepo.createTask(data, currentProject.getId());
  }

  public void importFromCsvOnCurrentProject(String csvPath) throws Exception {
    ProjectDb currentProject = projectsRepo.getCurrentProject();
    if (currentProject == null) {
      throw new Exception("No current project set.");
    }

    taskRepo.importFromCsv(csvPath, currentProject.getId());
  }
}
