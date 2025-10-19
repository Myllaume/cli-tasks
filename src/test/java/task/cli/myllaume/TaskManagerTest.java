package task.cli.myllaume;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.Test;
import task.cli.myllaume.db.ProjectsRepository;
import task.cli.myllaume.db.TaskManager;

public class TaskManagerTest {

  @Test
  public void testCreateTaskOnCurrentProjectSuccess() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    ProjectsRepository projectsRepo = new ProjectsRepository(dbPath);
    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    projectsRepo.initTables();

    projectsRepo.insertDefaultProjectIfNoneExists(ProjectData.of("Default Project", Instant.now()));

    TaskManager taskManager = new TaskManager(taskRepo, projectsRepo);

    Instant now = Instant.now();
    Instant dueDate = now.plusSeconds(86400);
    TaskData taskData = TaskData.of("Test Task", false, TaskPriority.MEDIUM, now, dueDate, null);

    Task createdTask = taskManager.createTaskOnCurrentProject(taskData);

    assertNotNull(createdTask);
    assertEquals("Test Task", createdTask.getDescription());
    assertTrue(createdTask.getId() > 0);
    assertEquals(TaskPriority.MEDIUM, createdTask.getPriority());
    assertFalse(createdTask.getCompleted());
  }

  @Test
  public void testCreateTaskOnCurrentProjectWithoutCurrentProject() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    ProjectsRepository projectsRepo = new ProjectsRepository(dbPath);
    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    projectsRepo.initTables();

    TaskManager taskManager = new TaskManager(taskRepo, projectsRepo);

    Instant now = Instant.now();
    TaskData taskData = TaskData.of("Test Task", false, TaskPriority.MEDIUM, now, null, null);

    try {
      taskManager.createTaskOnCurrentProject(taskData);
      fail("Should have thrown Exception for no current project");
    } catch (Exception e) {
      assertEquals("No current project set.", e.getMessage());
    }
  }

  @Test
  public void testCreateMultipleTasksOnCurrentProject() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    ProjectsRepository projectsRepo = new ProjectsRepository(dbPath);
    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    projectsRepo.initTables();

    projectsRepo.insertDefaultProjectIfNoneExists(ProjectData.of("Default Project", Instant.now()));

    TaskManager taskManager = new TaskManager(taskRepo, projectsRepo);

    Instant now = Instant.now();
    Task task1 =
        taskManager.createTaskOnCurrentProject(
            TaskData.of("Task 1", false, TaskPriority.HIGH, now, null, null));
    Task task2 =
        taskManager.createTaskOnCurrentProject(
            TaskData.of("Task 2", false, TaskPriority.LOW, now, null, null));
    Task task3 =
        taskManager.createTaskOnCurrentProject(
            TaskData.of("Task 3", false, TaskPriority.MEDIUM, now, null, null));

    assertNotNull(task1);
    assertNotNull(task2);
    assertNotNull(task3);
    assertEquals("Task 1", task1.getDescription());
    assertEquals("Task 2", task2.getDescription());
    assertEquals("Task 3", task3.getDescription());
    assertTrue(task1.getId() != task2.getId());
    assertTrue(task2.getId() != task3.getId());
  }

  @Test
  public void testCreateTaskWithDifferentPriorities() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    ProjectsRepository projectsRepo = new ProjectsRepository(dbPath);
    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    projectsRepo.initTables();

    projectsRepo.insertDefaultProjectIfNoneExists(ProjectData.of("Default Project", Instant.now()));

    TaskManager taskManager = new TaskManager(taskRepo, projectsRepo);

    Instant now = Instant.now();

    Task lowPriorityTask =
        taskManager.createTaskOnCurrentProject(
            TaskData.of("Low Priority Task", false, TaskPriority.LOW, now, null, null));
    Task mediumPriorityTask =
        taskManager.createTaskOnCurrentProject(
            TaskData.of("Medium Priority Task", false, TaskPriority.MEDIUM, now, null, null));
    Task highPriorityTask =
        taskManager.createTaskOnCurrentProject(
            TaskData.of("High Priority Task", false, TaskPriority.HIGH, now, null, null));

    assertEquals(TaskPriority.LOW, lowPriorityTask.getPriority());
    assertEquals(TaskPriority.MEDIUM, mediumPriorityTask.getPriority());
    assertEquals(TaskPriority.HIGH, highPriorityTask.getPriority());
  }

  @Test
  public void testCreateTaskAfterChangingCurrentProject() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    ProjectsRepository projectsRepo = new ProjectsRepository(dbPath);
    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    projectsRepo.initTables();

    projectsRepo.insertDefaultProjectIfNoneExists(ProjectData.of("Project 1", Instant.now()));
    ProjectDb project2 = projectsRepo.createProject(ProjectData.of("Project 2", Instant.now()));

    TaskManager taskManager = new TaskManager(taskRepo, projectsRepo);

    Instant now = Instant.now();
    Task task1 =
        taskManager.createTaskOnCurrentProject(
            TaskData.of("Task 1", false, TaskPriority.MEDIUM, now, null, null));

    projectsRepo.updateCurrentProject(project2.getId());

    Task task2 =
        taskManager.createTaskOnCurrentProject(
            TaskData.of("Task 2", false, TaskPriority.HIGH, now, null, null));

    assertNotNull(task1);
    assertNotNull(task2);
    assertNotEquals(task1.getId(), task2.getId());
  }

  @Test
  public void testTaskManagerInitializesTablesCorrectly() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    ProjectsRepository projectsRepo = new ProjectsRepository(dbPath);
    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);

    projectsRepo.initTables();

    projectsRepo.insertDefaultProjectIfNoneExists(ProjectData.of("Default Project", Instant.now()));

    TaskManager taskManager = new TaskManager(taskRepo, projectsRepo);

    Instant now = Instant.now();
    Task task =
        taskManager.createTaskOnCurrentProject(
            TaskData.of("Test Task", false, TaskPriority.LOW, now, null, null));

    assertNotNull(task);
  }
}
