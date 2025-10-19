package task.cli.myllaume;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.Test;
import picocli.CommandLine;
import task.cli.myllaume.db.ProjectsRepository;
import task.cli.myllaume.db.TaskManager;

public class CommandClearTest {

  private ProjectData defaultProject = ProjectData.of("Default Project", Instant.now());

  @Test
  public void testRunThanCheckCanContinueToAddTask() throws Exception {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream oldErr = System.err;
    PrintStream oldOut = System.out;

    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TaskRepositorySqlite tasksRepo = new TaskRepositorySqlite(dbPath);
    ProjectsRepository projectsRepo = new ProjectsRepository(dbPath);
    projectsRepo.initTables();
    projectsRepo.insertDefaultProjectIfNoneExists(defaultProject);
    TaskManager manager = new TaskManager(tasksRepo, projectsRepo);

    try {
      System.setErr(new PrintStream(err));
      System.setOut(new PrintStream(out));

      CommandClear cmdClear = new CommandClear(projectsRepo);
      cmdClear.run();
      CommandAdd cmdAdd = new CommandAdd(manager);
      new CommandLine(cmdAdd).parseArgs("Test");
      cmdAdd.run();
    } finally {
      System.setErr(oldErr);
      System.setOut(oldOut);
    }

    assertEquals("", err.toString());
    assertTrue(out.toString().contains("Toutes les tâches ont été supprimées."));
  }
}
