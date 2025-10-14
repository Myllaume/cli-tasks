package task.cli.myllaume;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.Test;
import task.cli.myllaume.db.ProjectsRepository;
import task.cli.myllaume.db.TaskManager;

public class CommandDoneTest {

  private Task createTask(String dbPath, TaskData data) throws Exception {
    TaskRepositorySqlite repoTasks = new TaskRepositorySqlite(dbPath);
    ProjectsRepository repoProject = new ProjectsRepository(dbPath);
    repoTasks.initTables();
    if (!repoProject.hasCurrentProject()) {
      repoProject.insertDefaultProjectIfNoneExists(
          ProjectData.of("Default Project", Instant.now()));
    }
    TaskManager manager = new TaskManager(repoTasks, repoProject);

    return manager.createTaskOnCurrentProject(data);
  }

  @Test
  public void testRunWithId() throws Exception {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream oldErr = System.err;
    PrintStream oldOut = System.out;

    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
    repo.initTables();

    TaskData taskData =
        TaskData.of("Test task", false, TaskPriority.LOW, Instant.now(), null, null);
    Task task = createTask(dbPath, taskData);

    try {
      System.setErr(new PrintStream(err));
      System.setOut(new PrintStream(out));

      CommandDone cmd = new CommandDone(repo);
      cmd.id = task.getId();
      cmd.completeLastAdded = false;
      cmd.run();
    } finally {
      System.setErr(oldErr);
      System.setOut(oldOut);
    }

    assertEquals("", err.toString());
    assertEquals("Tâche '1. Test task' marquée comme terminée.\n", out.toString());

    Task updatedTask = repo.getTask(task.getId());
    assertTrue(updatedTask.getCompleted());
  }

  @Test
  public void testRunWithLastOption() throws Exception {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream oldErr = System.err;
    PrintStream oldOut = System.out;

    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
    repo.initTables();

    TaskData firstTaskData =
        TaskData.of("First task", false, TaskPriority.LOW, Instant.now(), null, null);
    Task firstTask = createTask(dbPath, firstTaskData);
    TaskData lastTaskData =
        TaskData.of("Last task", false, TaskPriority.HIGH, Instant.now(), null, null);
    Task lastTask = createTask(dbPath, lastTaskData);
    assertFalse(lastTask.getCompleted());

    try {
      System.setErr(new PrintStream(err));
      System.setOut(new PrintStream(out));

      CommandDone cmd = new CommandDone(repo);
      cmd.completeLastAdded = true;
      cmd.run();
    } finally {
      System.setErr(oldErr);
      System.setOut(oldOut);
    }

    assertEquals("", err.toString());
    assertEquals("Tâche '2. Last task' marquée comme terminée.\n", out.toString());

    Task updatedFirstTask = repo.getTask(firstTask.getId());
    assertFalse(updatedFirstTask.getCompleted());
    Task updatedLastTask = repo.getTask(lastTask.getId());
    assertTrue(updatedLastTask.getCompleted());
  }

  @Test
  public void testRunWithAlreadyCompletedTask() throws Exception {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream oldErr = System.err;
    PrintStream oldOut = System.out;

    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
    repo.initTables();

    TaskData data =
        TaskData.of("Completed task", true, TaskPriority.LOW, Instant.now(), null, Instant.now());
    Task task = createTask(dbPath, data);

    try {
      System.setErr(new PrintStream(err));
      System.setOut(new PrintStream(out));

      CommandDone cmd = new CommandDone(repo);
      cmd.id = task.getId();
      cmd.completeLastAdded = false;
      cmd.run();
    } finally {
      System.setErr(oldErr);
      System.setOut(oldOut);
    }

    assertEquals("", err.toString());
    assertEquals("Tâche '1. Completed task' est déjà terminée.\n", out.toString());

    Task updatedTask = repo.getTask(task.getId());
    assertTrue(updatedTask.getCompleted());
  }

  @Test
  public void testRunWithNonExistentId() throws Exception {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream oldErr = System.err;
    PrintStream oldOut = System.out;

    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
    repo.initTables();

    try {
      System.setErr(new PrintStream(err));
      System.setOut(new PrintStream(out));

      CommandDone cmd = new CommandDone(repo);
      cmd.id = 999; // ID inexistant
      cmd.completeLastAdded = false;
      cmd.run();
    } finally {
      System.setErr(oldErr);
      System.setOut(oldOut);
    }

    assertEquals("", err.toString());
    assertEquals("Aucune tâche trouvée à marquer comme terminée.\n", out.toString());
  }

  @Test
  public void testRunWithLastOptionButNoTasks() throws Exception {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream oldErr = System.err;
    PrintStream oldOut = System.out;

    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
    repo.initTables();

    try {
      System.setErr(new PrintStream(err));
      System.setOut(new PrintStream(out));

      CommandDone cmd = new CommandDone(repo);
      cmd.completeLastAdded = true;
      cmd.run();
    } finally {
      System.setErr(oldErr);
      System.setOut(oldOut);
    }

    assertEquals("", err.toString());
    assertEquals("Aucune tâche trouvée à marquer comme terminée.\n", out.toString());
  }
}
