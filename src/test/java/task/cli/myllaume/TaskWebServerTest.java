package task.cli.myllaume;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.After;
import org.junit.Test;
import task.cli.myllaume.db.ProjectsRepository;

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
    TaskHtmlRenderer mockRenderer = mock(TaskHtmlRenderer.class);

    server = new TaskWebServer(8080, mockRepo, mockRenderer);

    assertEquals("http://localhost:8080", server.getUrl());
  }

  @Test
  public void testGetUrlWithDifferentPort() {
    TaskRepositorySqlite mockRepo = mock(TaskRepositorySqlite.class);
    TaskHtmlRenderer mockRenderer = mock(TaskHtmlRenderer.class);

    server = new TaskWebServer(3000, mockRepo, mockRenderer);

    assertEquals("http://localhost:3000", server.getUrl());
  }

  @Test
  public void testStartCreatesServer() throws Exception {
    TaskRepositorySqlite mockRepo = mock(TaskRepositorySqlite.class);
    TaskHtmlRenderer mockRenderer = mock(TaskHtmlRenderer.class);

    server = new TaskWebServer(8765, mockRepo, mockRenderer);
    server.start();

    assertNotNull(server);
  }

  @Test
  public void testStopDoesNotThrowWhenServerNotStarted() {
    TaskRepositorySqlite mockRepo = mock(TaskRepositorySqlite.class);
    TaskHtmlRenderer mockRenderer = mock(TaskHtmlRenderer.class);

    server = new TaskWebServer(8080, mockRepo, mockRenderer);

    server.stop(0);
  }

  @Test
  public void testServerStartsAndServesHomePage() throws Exception {

    Path tempDir = Files.createTempDirectory("test-db");
    tempDir.toFile().deleteOnExit();
    String dbPath = tempDir.toString();

    TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);

    repo.initTables();
    projectRepo.insertDefaultProjectIfNoneExists(ProjectData.of("Default Project", Instant.now()));

    TaskData taskData =
        TaskData.of(
            "Test task for web server", false, TaskPriority.HIGH, Instant.now(), null, null);
    repo.createTask(taskData, 1);

    TaskHtmlRenderer renderer = new TaskHtmlRenderer(9876);
    server = new TaskWebServer(9876, repo, renderer);

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
    assertTrue(content.contains("Serveur lancé sur le port 9876"));

    conn.disconnect();
  }

  @Test
  public void testServerServes404ForUnknownPath() throws Exception {

    Path tempDir = Files.createTempDirectory("test-db");
    tempDir.toFile().deleteOnExit();
    String dbPath = tempDir.toString();

    TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);

    repo.initTables();
    projectRepo.insertDefaultProjectIfNoneExists(ProjectData.of("Default Project", Instant.now()));

    TaskHtmlRenderer renderer = new TaskHtmlRenderer(9877);
    server = new TaskWebServer(9877, repo, renderer);

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

    TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);

    repo.initTables();
    projectRepo.insertDefaultProjectIfNoneExists(ProjectData.of("Default Project", Instant.now()));

    TaskHtmlRenderer renderer = new TaskHtmlRenderer(9878);
    server = new TaskWebServer(9878, repo, renderer);

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

    TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);

    repo.initTables();
    projectRepo.insertDefaultProjectIfNoneExists(ProjectData.of("Default Project", Instant.now()));

    TaskHtmlRenderer renderer = new TaskHtmlRenderer(9880);
    server = new TaskWebServer(9880, repo, renderer);

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

    TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);

    repo.initTables();
    projectRepo.insertDefaultProjectIfNoneExists(ProjectData.of("Default Project", Instant.now()));

    TaskHtmlRenderer mockRenderer = mock(TaskHtmlRenderer.class);
    when(mockRenderer.renderHome(anyList())).thenReturn("<html><body>MOCKED PAGE</body></html>");

    server = new TaskWebServer(9881, repo, mockRenderer);
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

    TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
    ProjectsRepository projectRepo = new ProjectsRepository(dbPath);

    repo.initTables();
    projectRepo.insertDefaultProjectIfNoneExists(ProjectData.of("Default Project", Instant.now()));

    TaskHtmlRenderer mockRenderer = mock(TaskHtmlRenderer.class);
    when(mockRenderer.render404()).thenReturn("<html><body>MOCKED 404</body></html>");

    server = new TaskWebServer(9882, repo, mockRenderer);
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
}
