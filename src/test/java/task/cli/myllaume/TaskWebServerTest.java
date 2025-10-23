package task.cli.myllaume;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import task.cli.myllaume.db.ProjectsRepository;
import task.cli.myllaume.db.TaskManager;

public class TaskWebServerTest {

  private TaskWebServer server;

  @After
  public void tearDown() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  public void testGetUrl() {
    TaskRepositorySqlite mockRepo = mock(TaskRepositorySqlite.class);
    TaskManager mockManager = mock(TaskManager.class);
    TaskHtmlRenderer mockRenderer = mock(TaskHtmlRenderer.class);

    server = new TaskWebServer(8080, mockRepo, mockRenderer, mockManager);

    assertEquals("http://localhost:8080", server.getUrl());
  }

  @Test
  public void testGetUrlWithDifferentPort() {
    TaskRepositorySqlite mockRepo = mock(TaskRepositorySqlite.class);
    TaskManager mockManager = mock(TaskManager.class);
    TaskHtmlRenderer mockRenderer = mock(TaskHtmlRenderer.class);

    server = new TaskWebServer(3000, mockRepo, mockRenderer, mockManager);

    assertEquals("http://localhost:3000", server.getUrl());
  }

  @Test
  public void testStartCreatesServer() throws Exception {
    TaskRepositorySqlite mockRepo = mock(TaskRepositorySqlite.class);
    TaskManager mockManager = mock(TaskManager.class);
    TaskHtmlRenderer mockRenderer = mock(TaskHtmlRenderer.class);

    server = new TaskWebServer(8765, mockRepo, mockRenderer, mockManager);
    server.start();

    assertNotNull(server);
  }

  @Test
  public void testStopDoesNotThrowWhenServerNotStarted() {
    TaskRepositorySqlite mockRepo = mock(TaskRepositorySqlite.class);
    TaskManager mockManager = mock(TaskManager.class);
    TaskHtmlRenderer mockRenderer = mock(TaskHtmlRenderer.class);

    server = new TaskWebServer(8080, mockRepo, mockRenderer, mockManager);

    server.stop(0);
  }

  @Test
  public void testServerStartsAndServesHomePage() throws Exception {

    Path tempDir = Files.createTempDirectory("test-db");
    tempDir.toFile().deleteOnExit();
    String dbPath = tempDir.toString();

    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);
    TaskManager taskManager = new TaskManager(taskRepo, projectRepo);

    taskRepo.initTables();
    projectRepo.insertDefaultProjectIfNoneExists(ProjectData.of("Default Project", Instant.now()));

    TaskData taskData =
        TaskData.of(
            "Test task for web server", false, TaskPriority.HIGH, Instant.now(), null, null);
    taskRepo.createTask(taskData, 1);

    TaskHtmlRenderer renderer = new TaskHtmlRenderer(9876);
    server = new TaskWebServer(9876, taskRepo, renderer, taskManager);

    server.start();

    Thread.sleep(200);

    URI uri = new URI("http://localhost:9876/");
    URL url = uri.toURL();
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");

    int responseCode = conn.getResponseCode();
    assertEquals(200, responseCode);

    InputStream is = conn.getInputStream();
    String content = new String(is.readAllBytes());
    is.close();

    assertTrue(content.contains("<!DOCTYPE html>"));
    assertTrue(content.contains("Test task for web server"));

    conn.disconnect();
  }

  @Test
  public void testServerServes404ForUnknownPath() throws Exception {

    Path tempDir = Files.createTempDirectory("test-db");
    tempDir.toFile().deleteOnExit();
    String dbPath = tempDir.toString();

    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);
    TaskManager taskManager = new TaskManager(taskRepo, projectRepo);

    taskRepo.initTables();
    projectRepo.insertDefaultProjectIfNoneExists(ProjectData.of("Default Project", Instant.now()));

    TaskHtmlRenderer renderer = new TaskHtmlRenderer(9877);
    server = new TaskWebServer(9877, taskRepo, renderer, taskManager);

    server.start();
    Thread.sleep(200);

    URI uri = new URI("http://localhost:9877/unknown-path");
    URL url = uri.toURL();
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");

    int responseCode = conn.getResponseCode();
    assertEquals(404, responseCode);

    InputStream is = conn.getErrorStream();
    String content = new String(is.readAllBytes());
    is.close();

    assertTrue(content.contains("404 - Page non trouvée"));

    conn.disconnect();
  }

  @Test
  public void testServerServesStaticCssFile() throws Exception {

    Path tempDir = Files.createTempDirectory("test-db");
    tempDir.toFile().deleteOnExit();
    String dbPath = tempDir.toString();

    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);
    TaskManager taskManager = new TaskManager(taskRepo, projectRepo);

    taskRepo.initTables();
    projectRepo.insertDefaultProjectIfNoneExists(ProjectData.of("Default Project", Instant.now()));

    TaskHtmlRenderer renderer = new TaskHtmlRenderer(9878);
    server = new TaskWebServer(9878, taskRepo, renderer, taskManager);

    server.start();
    Thread.sleep(200);

    URI uri = new URI("http://localhost:9878/styles.css");
    URL url = uri.toURL();
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");

    int responseCode = conn.getResponseCode();
    assertEquals(200, responseCode);

    String contentType = conn.getHeaderField("Content-Type");
    assertTrue(contentType.contains("text/css"));

    InputStream is = conn.getInputStream();
    String content = new String(is.readAllBytes());
    is.close();

    assertNotNull(content);
    assertTrue(content.length() > 0);

    conn.disconnect();
  }

  @Test
  public void testServerStopsCleanly() throws Exception {
    Path tempDir = Files.createTempDirectory("test-db");
    tempDir.toFile().deleteOnExit();
    String dbPath = tempDir.toString();

    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);
    TaskManager taskManager = new TaskManager(taskRepo, projectRepo);

    taskRepo.initTables();
    projectRepo.insertDefaultProjectIfNoneExists(ProjectData.of("Default Project", Instant.now()));

    TaskHtmlRenderer renderer = new TaskHtmlRenderer(9880);
    server = new TaskWebServer(9880, taskRepo, renderer, taskManager);

    server.start();
    Thread.sleep(200);

    server.stop(0);

    Thread.sleep(200);

    try {
      URI uri = new URI("http://localhost:9880/");
      URL url = uri.toURL();
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setConnectTimeout(1000);
      conn.setRequestMethod("GET");
      conn.getResponseCode();

      fail("Le serveur devrait être arrêté");
    } catch (IOException e) {

      assertTrue(true);
    }
  }

  @Test
  public void testServerUsesRendererForHomePage() throws Exception {
    Path tempDir = Files.createTempDirectory("test-db");
    tempDir.toFile().deleteOnExit();
    String dbPath = tempDir.toString();

    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);
    TaskManager taskManager = new TaskManager(taskRepo, projectRepo);

    taskRepo.initTables();
    projectRepo.insertDefaultProjectIfNoneExists(ProjectData.of("Default Project", Instant.now()));

    TaskHtmlRenderer mockRenderer = mock(TaskHtmlRenderer.class);
    when(mockRenderer.renderHome(anyList())).thenReturn("<html><body>MOCKED PAGE</body></html>");

    server = new TaskWebServer(9881, taskRepo, mockRenderer, taskManager);
    server.start();
    Thread.sleep(200);

    URI uri = new URI("http://localhost:9881/");
    URL url = uri.toURL();
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");

    InputStream is = conn.getInputStream();
    String content = new String(is.readAllBytes());
    is.close();

    assertTrue(content.contains("MOCKED PAGE"));
    verify(mockRenderer, times(1)).renderHome(anyList());

    conn.disconnect();
  }

  @Test
  public void testServerUsesRendererFor404() throws Exception {
    Path tempDir = Files.createTempDirectory("test-db");
    tempDir.toFile().deleteOnExit();
    String dbPath = tempDir.toString();

    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);
    TaskManager taskManager = new TaskManager(taskRepo, projectRepo);

    taskRepo.initTables();
    projectRepo.insertDefaultProjectIfNoneExists(ProjectData.of("Default Project", Instant.now()));

    TaskHtmlRenderer mockRenderer = mock(TaskHtmlRenderer.class);
    when(mockRenderer.render404()).thenReturn("<html><body>MOCKED 404</body></html>");

    server = new TaskWebServer(9882, taskRepo, mockRenderer, taskManager);
    server.start();
    Thread.sleep(200);

    URI uri = new URI("http://localhost:9882/notfound");
    URL url = uri.toURL();
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");

    int responseCode = conn.getResponseCode();
    assertEquals(404, responseCode);

    InputStream is = responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
    String content = new String(is.readAllBytes());
    is.close();

    assertTrue(content.contains("MOCKED 404"));
    verify(mockRenderer, times(1)).render404();

    conn.disconnect();
  }

  @Test
  public void testPostFormDataWithSingleField() throws Exception {
    Path tempDir = Files.createTempDirectory("test-db");
    tempDir.toFile().deleteOnExit();
    String dbPath = tempDir.toString();

    TaskRepositorySqlite taskRepo = new TaskRepositorySqlite(dbPath);
    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);
    TaskManager taskManager = new TaskManager(taskRepo, projectRepo);

    taskRepo.initTables();
    projectRepo.insertDefaultProjectIfNoneExists(ProjectData.of("Default Project", Instant.now()));

    TaskHtmlRenderer renderer = new TaskHtmlRenderer(9883);
    server = new TaskWebServer(9883, taskRepo, renderer, taskManager);
    server.start();
    Thread.sleep(200);

    URI uri = new URI("http://localhost:9883/add-tasks");
    URL url = uri.toURL();
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setInstanceFollowRedirects(false); // Ne pas suivre automatiquement les redirections
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);
    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

    String formData = "description=Ma+nouvelle+tache&priority=2";
    OutputStream os = conn.getOutputStream();
    os.write(formData.getBytes(StandardCharsets.UTF_8));
    os.flush();
    os.close();

    int responseCode = conn.getResponseCode();
    assertEquals(302, responseCode); // Redirection

    String location = conn.getHeaderField("Location");
    assertEquals("/", location);

    conn.disconnect();

    List<Task> tasks = taskRepo.getTasks(100);
    assertEquals(tasks.size(), 1);
    assertEquals(tasks.get(0).getCompleted(), false);
    assertEquals(tasks.get(0).getDescription(), "Ma nouvelle tache");
    assertEquals(tasks.get(0).getPriority(), TaskPriority.MEDIUM);
  }
}
