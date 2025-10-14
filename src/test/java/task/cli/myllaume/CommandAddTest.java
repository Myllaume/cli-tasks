package task.cli.myllaume;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import org.junit.Test;
import picocli.CommandLine;
import task.cli.myllaume.db.ProjectsRepository;
import task.cli.myllaume.db.TaskManager;

public class CommandAddTest {
  private TaskManager getManager(String dbPath) throws Exception {
    TaskRepositorySqlite repoTasks = new TaskRepositorySqlite(dbPath);
    ProjectsRepository repoProject = new ProjectsRepository(dbPath);
    repoTasks.initTables();
    repoProject.insertDefaultProjectIfNoneExists(ProjectData.of("Default Project", Instant.now()));
    TaskManager manager = new TaskManager(repoTasks, repoProject);
    return manager;
  }

  @Test
  public void testRunWithDefaultOptions() throws Exception {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream oldErr = System.err;
    PrintStream oldOut = System.out;

    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TaskManager manager = getManager(dbPath);

    try {
      System.setErr(new PrintStream(err));
      System.setOut(new PrintStream(out));

      CommandAdd cmd = new CommandAdd(manager);
      new CommandLine(cmd).parseArgs("Test");
      cmd.run();
    } finally {
      System.setErr(oldErr);
      System.setOut(oldOut);
    }

    assertEquals("", err.toString());
    assertEquals("La tâche '1. Test' a été ajoutée.\n", out.toString());

    TaskRepositorySqlite repoTasks = new TaskRepositorySqlite(dbPath);
    ArrayList<Task> tasks = repoTasks.getTasks(10);
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals("Test", task.getDescription());
    assertFalse(task.getCompleted());
    assertEquals(TaskPriority.LOW, task.getPriority());
  }

  @Test
  public void testRunCompleted() throws Exception {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream oldErr = System.err;
    PrintStream oldOut = System.out;

    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TaskManager manager = getManager(dbPath);

    try {
      System.setErr(new PrintStream(err));
      System.setOut(new PrintStream(out));

      CommandAdd cmd = new CommandAdd(manager);
      cmd.description = "Test";
      cmd.completed = true;
      cmd.priority = 3;
      cmd.run();
    } finally {
      System.setErr(oldErr);
      System.setOut(oldOut);
    }

    assertEquals("", err.toString());
    assertEquals("La tâche '1. Test' a été ajoutée.\n", out.toString());

    TaskRepositorySqlite repoTasks = new TaskRepositorySqlite(dbPath);
    ArrayList<Task> tasks = repoTasks.getTasks(10);
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals("Test", task.getDescription());
    assertTrue(task.getCompleted());
    assertEquals(TaskPriority.HIGH, task.getPriority());
  }
}
