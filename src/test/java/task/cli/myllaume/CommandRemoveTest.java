package task.cli.myllaume;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import org.junit.Test;
import task.cli.myllaume.db.ProjectsRepository;
import task.cli.myllaume.db.TaskManager;

public class CommandRemoveTest {

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
  public void testRun() throws Exception {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream oldErr = System.err;
    PrintStream oldOut = System.out;

    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
    repo.initTables();

    Instant now = Instant.now();
    TaskData taskOneData = TaskData.of("One", false, TaskPriority.LOW, now, null, null);
    createTask(dbPath, taskOneData);
    TaskData taskTwoData = TaskData.of("Two", true, TaskPriority.LOW, now, null, now);
    Task task2 = createTask(dbPath, taskTwoData);

    try {
      System.setErr(new PrintStream(err));
      System.setOut(new PrintStream(out));

      CommandRemove cmd = new CommandRemove(repo);
      cmd.id = String.valueOf(task2.getId());
      cmd.run();
    } finally {
      System.setErr(oldErr);
      System.setOut(oldOut);
    }

    assertEquals("", err.toString());
    assertEquals("La tâche " + task2.getId() + " a été supprimée.\n", out.toString());

    ArrayList<Task> tasks = repo.getTasks(10);
    assertEquals(1, tasks.size());
    assertEquals("One", tasks.get(0).getDescription());
  }

  @Test
  public void testRunFail() throws Exception {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream oldErr = System.err;
    PrintStream oldOut = System.out;

    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
    repo.initTables();

    Instant now = Instant.now();
    TaskData taskOneData = TaskData.of("One", false, TaskPriority.LOW, now, null, null);
    createTask(dbPath, taskOneData);
    TaskData taskTwoData = TaskData.of("Two", true, TaskPriority.LOW, now, null, now);
    createTask(dbPath, taskTwoData);

    try {
      System.setErr(new PrintStream(err));
      System.setOut(new PrintStream(out));

      CommandRemove cmd = new CommandRemove(repo);
      cmd.id = "100"; // does not exist
      cmd.run();
    } finally {
      System.setErr(oldErr);
      System.setOut(oldOut);
    }

    assertEquals("", err.toString());
    assertEquals("Erreur lors de la suppression de la tâche 100.\n", out.toString());

    ArrayList<Task> tasks = repo.getTasks(10);
    assertEquals(2, tasks.size());
  }
}
