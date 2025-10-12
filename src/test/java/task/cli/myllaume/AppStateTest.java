package task.cli.myllaume;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.Test;
import task.cli.myllaume.config.AppConfigRepository;
import task.cli.myllaume.db.ProjectsRepository;

public class AppStateTest {

  @Test
  public void testIsFirstLaunch_whenNoConfigAndNoProjects_returnsTrue() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    AppConfigRepository config = new AppConfigRepository(dbPath);
    ProjectsRepository projectsRepo = new ProjectsRepository(dbPath);
    projectsRepo.initTables();

    AppState appState = new AppState(config, projectsRepo);

    assertTrue(appState.isFirstLaunch());
  }

  @Test
  public void testIsFirstLaunch_whenConfigExistsAndHasProjects_returnsFalse() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    AppConfigRepository config = new AppConfigRepository(dbPath);
    ProjectsRepository projectsRepo = new ProjectsRepository(dbPath);

    config.init();
    projectsRepo.initTables();
    projectsRepo.insertDefaultProjectIfNoneExists(ProjectData.of("Test Project", Instant.now()));

    AppState appState = new AppState(config, projectsRepo);

    assertFalse(appState.isFirstLaunch());
  }

  @Test
  public void testIsFirstLaunch_whenNoConfigButHasProjects_throwsException() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    AppConfigRepository config = new AppConfigRepository(dbPath);
    ProjectsRepository projectsRepo = new ProjectsRepository(dbPath);

    projectsRepo.initTables();
    projectsRepo.insertDefaultProjectIfNoneExists(ProjectData.of("Test Project", Instant.now()));

    AppState appState = new AppState(config, projectsRepo);

    try {
      appState.isFirstLaunch();
      fail("Should have thrown IllegalStateException");
    } catch (IllegalStateException e) {
      assertEquals("Config file is missing but projects exist in database.", e.getMessage());
    }
  }

  @Test
  public void testIsFirstLaunch_whenConfigExistsButNoProjects_throwsException() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    AppConfigRepository config = new AppConfigRepository(dbPath);
    ProjectsRepository projectsRepo = new ProjectsRepository(dbPath);

    config.init();

    AppState appState = new AppState(config, projectsRepo);

    try {
      appState.isFirstLaunch();
      fail("Should have thrown IllegalStateException");
    } catch (IllegalStateException e) {
      assertEquals("Config file exists but no projects found in database.", e.getMessage());
    }
  }

  @Test
  public void testFirstLaunchSetup_success() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    AppConfigRepository config = new AppConfigRepository(dbPath);
    ProjectsRepository projectsRepo = new ProjectsRepository(dbPath);
    projectsRepo.initTables();

    AppState appState = new AppState(config, projectsRepo);

    assertTrue(appState.isFirstLaunch());

    appState.firstLaunchSetup();

    assertFalse(appState.isFirstLaunch());
    assertTrue(config.fileExists());
    assertFalse(projectsRepo.isEmpty());
  }

  @Test
  public void testFirstLaunchSetup_whenNotFirstLaunch_throwsException() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    AppConfigRepository config = new AppConfigRepository(dbPath);
    ProjectsRepository projectsRepo = new ProjectsRepository(dbPath);

    AppState appState = new AppState(config, projectsRepo);
    appState.firstLaunchSetup();

    try {
      appState.firstLaunchSetup();
      fail("Should have thrown IllegalStateException");
    } catch (IllegalStateException e) {
      assertEquals(
          "Cannot run first launch setup: application is already initialized.", e.getMessage());
    }
  }

  @Test
  public void testFirstLaunchSetup_whenConfigInitFails_throwsException() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String readOnlyDir = tempDir.toString() + File.separator + "readonly";
    File readOnlyFile = new File(readOnlyDir);
    readOnlyFile.mkdirs();
    readOnlyFile.setReadOnly();

    String dbPath = tempDir.toString();
    AppConfigRepository config = new AppConfigRepository(readOnlyDir);
    ProjectsRepository projectsRepo = new ProjectsRepository(dbPath);

    AppState appState = new AppState(config, projectsRepo);

    try {
      appState.firstLaunchSetup();
      fail("Should have thrown IllegalStateException");
    } catch (IllegalStateException e) {
      assertTrue(e.getMessage().contains("First launch setup failed"));
      assertTrue(e.getMessage().contains("inconsistent state"));
      assertNotNull(e.getCause());
    } finally {
      readOnlyFile.setWritable(true);
    }
  }
}
