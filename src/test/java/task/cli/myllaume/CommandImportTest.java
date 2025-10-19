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

public class CommandImportTest {

  private ProjectData defaultProject = ProjectData.of("Default Project", Instant.now());

  @Test
  public void testRunWithId() throws Exception {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream oldErr = System.err;
    PrintStream oldOut = System.out;

    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TaskRepositorySqlite tasksRepo = new TaskRepositorySqlite(dbPath);
    tasksRepo.initTables();
    ProjectsRepository projectsRepository = new ProjectsRepository(dbPath);
    projectsRepository.insertDefaultProjectIfNoneExists(defaultProject);
    TaskManager manager = new TaskManager(tasksRepo, projectsRepository);

    try {
      System.setErr(new PrintStream(err));
      System.setOut(new PrintStream(out));

      CommandImport cmd = new CommandImport(manager);
      cmd.filePath = "src/test/resources/many.csv";
      cmd.run();
    } finally {
      System.setErr(oldErr);
      System.setOut(oldOut);
    }

    assertEquals("", err.toString());
    assertEquals("52 tâches ont été importées.\n", out.toString());
  }
}
