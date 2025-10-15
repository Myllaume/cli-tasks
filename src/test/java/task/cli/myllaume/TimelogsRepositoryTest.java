package task.cli.myllaume;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import org.junit.Test;
import task.cli.myllaume.db.ProjectsRepository;
import task.cli.myllaume.db.TimelogsRepository;

public class TimelogsRepositoryTest {

  private ProjectData defaultProjectData = ProjectData.of("Default Project", Instant.now());

  @Test
  public void testCreateTimelogSuccess() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TimelogsRepository timelogRepo = new TimelogsRepository(dbPath);
    timelogRepo.initTables();

    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);
    ProjectDb project = projectRepo.insertDefaultProjectIfNoneExists(defaultProjectData);

    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    Instant now = Instant.now();
    TaskData taskData = TaskData.of("Test Task", false, TaskPriority.MEDIUM, now, null, null);
    Task task = taskRepo.createTask(taskData, project.getId());

    Instant start = Instant.now();
    Instant stop = start.plusSeconds(3600);
    TimelogDb timelog = timelogRepo.createTimelog(TimelogData.of(task.getId(), start, stop));

    assertTrue(timelog.getId() > 0);
    assertEquals(task.getId(), timelog.getTaskId());
    assertEquals(start.getEpochSecond(), timelog.getStartedAt().getEpochSecond());
    assertEquals(stop.getEpochSecond(), timelog.getStoppedAt().getEpochSecond());
    assertEquals(3600, timelog.getDurationSeconds());
  }

  @Test
  public void testGetTimelogSuccess() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TimelogsRepository timelogRepo = new TimelogsRepository(dbPath);
    timelogRepo.initTables();

    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);
    ProjectDb project = projectRepo.insertDefaultProjectIfNoneExists(defaultProjectData);

    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    Instant now = Instant.now();
    TaskData taskData = TaskData.of("Test Task", false, TaskPriority.MEDIUM, now, null, null);
    Task task = taskRepo.createTask(taskData, project.getId());

    Instant start = Instant.now();
    Instant stop = start.plusSeconds(1800);
    TimelogDb createdTimelog = timelogRepo.createTimelog(TimelogData.of(task.getId(), start, stop));

    TimelogDb retrievedTimelog = timelogRepo.getTimelog(createdTimelog.getId());

    assertNotNull(retrievedTimelog);
    assertEquals(createdTimelog.getId(), retrievedTimelog.getId());
    assertEquals(createdTimelog.getTaskId(), retrievedTimelog.getTaskId());
    assertEquals(
        createdTimelog.getStartedAt().getEpochSecond(),
        retrievedTimelog.getStartedAt().getEpochSecond());
    assertEquals(
        createdTimelog.getStoppedAt().getEpochSecond(),
        retrievedTimelog.getStoppedAt().getEpochSecond());
  }

  @Test
  public void testGetNonExistentTimelog() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TimelogsRepository timelogRepo = new TimelogsRepository(dbPath);
    timelogRepo.initTables();

    assertNull(timelogRepo.getTimelog(999));
  }

  @Test
  public void testRemoveTimelogSuccess() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TimelogsRepository timelogRepo = new TimelogsRepository(dbPath);
    timelogRepo.initTables();

    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);
    ProjectDb project = projectRepo.insertDefaultProjectIfNoneExists(defaultProjectData);

    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    Instant now = Instant.now();
    TaskData taskData = TaskData.of("Test Task", false, TaskPriority.MEDIUM, now, null, null);
    Task task = taskRepo.createTask(taskData, project.getId());

    Instant start = Instant.now();
    Instant stop = start.plusSeconds(2700);
    TimelogDb createdTimelog = timelogRepo.createTimelog(TimelogData.of(task.getId(), start, stop));
    int timelogId = createdTimelog.getId();

    TimelogDb removedTimelog = timelogRepo.removeTimelog(timelogId);

    assertEquals(createdTimelog.getId(), removedTimelog.getId());
    assertEquals(createdTimelog.getTaskId(), removedTimelog.getTaskId());
    assertNull(timelogRepo.getTimelog(timelogId));
  }

  @Test
  public void testRemoveNonExistentTimelog() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TimelogsRepository timelogRepo = new TimelogsRepository(dbPath);
    timelogRepo.initTables();

    try {
      timelogRepo.removeTimelog(999);
      fail("Should have thrown IllegalArgumentException for non-existent timelog");
    } catch (IllegalArgumentException e) {
      assertEquals("Timelog with ID 999 not found", e.getMessage());
    }
  }

  @Test
  public void testGetTimelogsByTask() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TimelogsRepository timelogRepo = new TimelogsRepository(dbPath);
    timelogRepo.initTables();

    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);
    ProjectDb project = projectRepo.insertDefaultProjectIfNoneExists(defaultProjectData);

    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    Instant now = Instant.now();
    TaskData taskData1 = TaskData.of("Task 1", false, TaskPriority.MEDIUM, now, null, null);
    TaskData taskData2 = TaskData.of("Task 2", false, TaskPriority.HIGH, now, null, null);
    Task task1 = taskRepo.createTask(taskData1, project.getId());
    Task task2 = taskRepo.createTask(taskData2, project.getId());

    Instant start1 = Instant.now();
    timelogRepo.createTimelog(TimelogData.of(task1.getId(), start1, start1.plusSeconds(1000)));
    timelogRepo.createTimelog(
        TimelogData.of(task1.getId(), start1.plusSeconds(2000), start1.plusSeconds(3000)));
    timelogRepo.createTimelog(
        TimelogData.of(task1.getId(), start1.plusSeconds(4000), start1.plusSeconds(5000)));
    timelogRepo.createTimelog(TimelogData.of(task2.getId(), start1, start1.plusSeconds(1500)));

    ArrayList<TimelogDb> timelogs = timelogRepo.getTimelogsByTask(task1.getId(), 10);

    assertEquals(3, timelogs.size());
    for (TimelogDb timelog : timelogs) {
      assertEquals(task1.getId(), timelog.getTaskId());
    }
  }

  @Test
  public void testGetTimelogsByTaskWithLimit() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TimelogsRepository timelogRepo = new TimelogsRepository(dbPath);
    timelogRepo.initTables();

    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);
    ProjectDb project = projectRepo.insertDefaultProjectIfNoneExists(defaultProjectData);

    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    Instant now = Instant.now();
    TaskData taskData = TaskData.of("Test Task", false, TaskPriority.MEDIUM, now, null, null);
    Task task = taskRepo.createTask(taskData, project.getId());

    Instant start = Instant.now();
    timelogRepo.createTimelog(TimelogData.of(task.getId(), start, start.plusSeconds(1000)));
    timelogRepo.createTimelog(
        TimelogData.of(task.getId(), start.plusSeconds(2000), start.plusSeconds(3000)));
    timelogRepo.createTimelog(
        TimelogData.of(task.getId(), start.plusSeconds(4000), start.plusSeconds(5000)));

    ArrayList<TimelogDb> timelogs = timelogRepo.getTimelogsByTask(task.getId(), 2);

    assertEquals(2, timelogs.size());
  }

  @Test
  public void testGetTimelogs() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TimelogsRepository timelogRepo = new TimelogsRepository(dbPath);
    timelogRepo.initTables();

    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);
    ProjectDb project = projectRepo.insertDefaultProjectIfNoneExists(defaultProjectData);

    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    Instant now = Instant.now();
    TaskData taskData = TaskData.of("Test Task", false, TaskPriority.MEDIUM, now, null, null);
    Task task = taskRepo.createTask(taskData, project.getId());

    Instant start = Instant.now();
    timelogRepo.createTimelog(TimelogData.of(task.getId(), start, start.plusSeconds(1000)));
    timelogRepo.createTimelog(
        TimelogData.of(task.getId(), start.plusSeconds(2000), start.plusSeconds(3000)));

    ArrayList<TimelogDb> timelogs = timelogRepo.getTimelogs(10);

    assertEquals(2, timelogs.size());
  }

  @Test
  public void testUpdateTimelog() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TimelogsRepository timelogRepo = new TimelogsRepository(dbPath);
    timelogRepo.initTables();

    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);
    ProjectDb project = projectRepo.insertDefaultProjectIfNoneExists(defaultProjectData);

    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    Instant now = Instant.now();
    TaskData taskData1 = TaskData.of("Task 1", false, TaskPriority.MEDIUM, now, null, null);
    TaskData taskData2 = TaskData.of("Task 2", false, TaskPriority.HIGH, now, null, null);
    Task task1 = taskRepo.createTask(taskData1, project.getId());
    Task task2 = taskRepo.createTask(taskData2, project.getId());

    Instant start = Instant.now();
    Instant stop = start.plusSeconds(3600);
    TimelogDb originalTimelog =
        timelogRepo.createTimelog(TimelogData.of(task1.getId(), start, stop));

    Instant newStart = start.plusSeconds(1000);
    Instant newStop = stop.plusSeconds(1000);
    TimelogData updatedData = TimelogData.of(task2.getId(), newStart, newStop);
    TimelogDb updatedTimelog = timelogRepo.updateTimelog(originalTimelog.getId(), updatedData);

    assertEquals(originalTimelog.getId(), updatedTimelog.getId());
    assertEquals(task2.getId(), updatedTimelog.getTaskId());
    assertEquals(newStart.getEpochSecond(), updatedTimelog.getStartedAt().getEpochSecond());
    assertEquals(newStop.getEpochSecond(), updatedTimelog.getStoppedAt().getEpochSecond());
  }

  @Test
  public void testUpdateNonExistentTimelog() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TimelogsRepository timelogRepo = new TimelogsRepository(dbPath);
    timelogRepo.initTables();

    Instant start = Instant.now();
    Instant stop = start.plusSeconds(3600);
    TimelogData data = TimelogData.of(1, start, stop);

    try {
      timelogRepo.updateTimelog(999, data);
      fail("Should have thrown IllegalArgumentException for non-existent timelog");
    } catch (IllegalArgumentException e) {
      assertEquals("Timelog with ID 999 not found", e.getMessage());
    }
  }

  @Test
  public void testGetTotalDurationForTask() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TimelogsRepository timelogRepo = new TimelogsRepository(dbPath);
    timelogRepo.initTables();

    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);
    ProjectDb project = projectRepo.insertDefaultProjectIfNoneExists(defaultProjectData);

    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    Instant now = Instant.now();
    TaskData taskData = TaskData.of("Test Task", false, TaskPriority.MEDIUM, now, null, null);
    Task task = taskRepo.createTask(taskData, project.getId());

    Instant start = Instant.now();
    timelogRepo.createTimelog(TimelogData.of(task.getId(), start, start.plusSeconds(1000)));
    timelogRepo.createTimelog(
        TimelogData.of(task.getId(), start.plusSeconds(2000), start.plusSeconds(4500)));
    timelogRepo.createTimelog(
        TimelogData.of(task.getId(), start.plusSeconds(5000), start.plusSeconds(6200)));

    long totalDuration = timelogRepo.getTotalDurationForTask(task.getId());

    assertEquals(4700, totalDuration); // 1000 + 2500 + 1200
  }

  @Test
  public void testGetTotalDurationForTaskWithNoTimelogs() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TimelogsRepository timelogRepo = new TimelogsRepository(dbPath);
    timelogRepo.initTables();

    long totalDuration = timelogRepo.getTotalDurationForTask(999);

    assertEquals(0, totalDuration);
  }

  @Test
  public void testCountTimelogsByTask() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TimelogsRepository timelogRepo = new TimelogsRepository(dbPath);
    timelogRepo.initTables();

    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);
    ProjectDb project = projectRepo.insertDefaultProjectIfNoneExists(defaultProjectData);

    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    Instant now = Instant.now();
    TaskData taskData1 = TaskData.of("Task 1", false, TaskPriority.MEDIUM, now, null, null);
    TaskData taskData2 = TaskData.of("Task 2", false, TaskPriority.HIGH, now, null, null);
    Task task1 = taskRepo.createTask(taskData1, project.getId());
    Task task2 = taskRepo.createTask(taskData2, project.getId());

    assertEquals(0, timelogRepo.countTimelogsByTask(task1.getId()));

    Instant start = Instant.now();
    timelogRepo.createTimelog(TimelogData.of(task1.getId(), start, start.plusSeconds(1000)));
    assertEquals(1, timelogRepo.countTimelogsByTask(task1.getId()));

    timelogRepo.createTimelog(
        TimelogData.of(task1.getId(), start.plusSeconds(2000), start.plusSeconds(3000)));
    assertEquals(2, timelogRepo.countTimelogsByTask(task1.getId()));

    timelogRepo.createTimelog(TimelogData.of(task2.getId(), start, start.plusSeconds(1500)));
    assertEquals(2, timelogRepo.countTimelogsByTask(task1.getId()));
    assertEquals(1, timelogRepo.countTimelogsByTask(task2.getId()));
  }

  @Test
  public void testCountTimelogs() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TimelogsRepository timelogRepo = new TimelogsRepository(dbPath);
    timelogRepo.initTables();

    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);
    ProjectDb project = projectRepo.insertDefaultProjectIfNoneExists(defaultProjectData);

    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    Instant now = Instant.now();
    TaskData taskData = TaskData.of("Test Task", false, TaskPriority.MEDIUM, now, null, null);
    Task task = taskRepo.createTask(taskData, project.getId());

    assertEquals(0, timelogRepo.countTimelogs());

    Instant start = Instant.now();
    timelogRepo.createTimelog(TimelogData.of(task.getId(), start, start.plusSeconds(1000)));
    assertEquals(1, timelogRepo.countTimelogs());

    timelogRepo.createTimelog(
        TimelogData.of(task.getId(), start.plusSeconds(2000), start.plusSeconds(3000)));
    assertEquals(2, timelogRepo.countTimelogs());
  }

  @Test
  public void testIsEmpty() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TimelogsRepository timelogRepo = new TimelogsRepository(dbPath);
    timelogRepo.initTables();

    assertTrue(timelogRepo.isEmpty());

    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);
    ProjectDb project = projectRepo.insertDefaultProjectIfNoneExists(defaultProjectData);

    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    Instant now = Instant.now();
    TaskData taskData = TaskData.of("Test Task", false, TaskPriority.MEDIUM, now, null, null);
    Task task = taskRepo.createTask(taskData, project.getId());

    Instant start = Instant.now();
    timelogRepo.createTimelog(TimelogData.of(task.getId(), start, start.plusSeconds(1000)));

    assertFalse(timelogRepo.isEmpty());
  }

  @Test
  public void testDeleteTaskCascadesTimelogs() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TimelogsRepository timelogRepo = new TimelogsRepository(dbPath);
    timelogRepo.initTables();

    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);
    ProjectDb project = projectRepo.insertDefaultProjectIfNoneExists(defaultProjectData);

    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    Instant now = Instant.now();
    TaskData taskData = TaskData.of("Test Task", false, TaskPriority.MEDIUM, now, null, null);
    Task task = taskRepo.createTask(taskData, project.getId());

    Instant start = Instant.now();
    timelogRepo.createTimelog(TimelogData.of(task.getId(), start, start.plusSeconds(1000)));
    timelogRepo.createTimelog(
        TimelogData.of(task.getId(), start.plusSeconds(2000), start.plusSeconds(3000)));

    assertEquals(2, timelogRepo.countTimelogs());

    taskRepo.removeTask(task.getId());

    assertEquals(0, timelogRepo.countTimelogs());
  }

  @Test
  public void testDeleteProjectCascadesTimelogs() throws Exception {
    Path tempDir = Files.createTempDirectory("tests");
    tempDir.toFile().deleteOnExit();

    String dbPath = tempDir.toString();
    TimelogsRepository timelogRepo = new TimelogsRepository(dbPath);
    timelogRepo.initTables();

    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);
    projectRepo.insertDefaultProjectIfNoneExists(defaultProjectData);
    ProjectDb project =
        projectRepo.createProject(ProjectData.of("Project to Delete", Instant.now()));

    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    Instant now = Instant.now();
    TaskData taskData = TaskData.of("Test Task", false, TaskPriority.MEDIUM, now, null, null);
    Task task = taskRepo.createTask(taskData, project.getId());

    Instant start = Instant.now();
    timelogRepo.createTimelog(TimelogData.of(task.getId(), start, start.plusSeconds(1000)));
    timelogRepo.createTimelog(
        TimelogData.of(task.getId(), start.plusSeconds(2000), start.plusSeconds(3000)));

    assertEquals(2, timelogRepo.countTimelogs());

    projectRepo.removeProject(project.getId());

    assertEquals(0, timelogRepo.countTimelogs());
  }
}
