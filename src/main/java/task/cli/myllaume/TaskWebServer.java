package task.cli.myllaume;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import task.cli.myllaume.db.TaskManager;

public class TaskWebServer implements WebServer {

  private final int port;
  private final TaskRepositorySqlite repository;
  private final TaskManager taskManager;
  private final TaskHtmlRenderer renderer;
  private HttpServer server;

  public TaskWebServer(
      int port,
      TaskRepositorySqlite repository,
      TaskHtmlRenderer renderer,
      TaskManager taskManager) {
    this.port = port;
    this.repository = repository;
    this.renderer = renderer;
    this.taskManager = taskManager;
  }

  @Override
  public void start() throws IOException {
    server = HttpServer.create(new InetSocketAddress(port), 0);
    server.createContext("/", this::handleRequest);
    server.start();
  }

  @Override
  public void stop(int delaySeconds) {
    if (server != null) {
      server.stop(delaySeconds);
    }
  }

  @Override
  public String getUrl() {
    return "http://localhost:" + port;
  }

  private void handleRequest(HttpExchange exchange) throws IOException {
    String path = exchange.getRequestURI().getPath();
    String method = exchange.getRequestMethod();

    try {
      if (Set.of("/styles.css", "/script.js").contains(path)) {
        serveStaticFile(exchange, path);
        return;
      }

      if (path.equals("/")) {
        serveHome(exchange);
        return;
      }

      if (path.equals("/form")) {
        serveForm(exchange);
        return;
      }

      if (path.equals("/add-tasks") && method.equals("POST")) {
        handleAddTask(exchange);
        return;
      }

      serve404(exchange);
    } catch (Exception e) {
      serve500(exchange);
    }
  }

  private void serveHome(HttpExchange exchange) throws Exception {
    List<Task> tasks = repository.getTasks(100);
    String html = renderer.renderHome(tasks);
    sendResponse(exchange, 200, "text/html; charset=UTF-8", html.getBytes());
  }

  private void serveForm(HttpExchange exchange) throws Exception {
    String html = renderer.renderForm();
    sendResponse(exchange, 200, "text/html; charset=UTF-8", html.getBytes());
  }

  private void handleAddTask(HttpExchange exchange) throws Exception {
    InputStream requestBody = exchange.getRequestBody();
    String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);

    Map<String, String> formData = parseFormData(body);
    String description = formData.get("description");

    if (description != null && !description.isEmpty()) {

      TaskData taskData =
          TaskData.of(description, false, TaskPriority.LOW, Instant.now(), null, null);

      taskManager.createTaskOnCurrentProject(taskData);
    }

    exchange.getResponseHeaders().set("Location", "/");
    exchange.sendResponseHeaders(302, -1);
  }

  private Map<String, String> parseFormData(String formData) {
    Map<String, String> result = new HashMap<>();
    if (formData == null || formData.isEmpty()) {
      return result;
    }

    String[] pairs = formData.split("&");
    for (String pair : pairs) {
      String[] keyValue = pair.split("=", 2);
      if (keyValue.length == 2) {
        String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
        String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
        result.put(key, value);
      }
    }
    return result;
  }

  private void serve404(HttpExchange exchange) throws IOException {
    String html = renderer.render404();
    sendResponse(exchange, 404, "text/html; charset=UTF-8", html.getBytes());
  }

  private void serve500(HttpExchange exchange) throws IOException {
    String html = renderer.render500();
    sendResponse(exchange, 500, "text/html; charset=UTF-8", html.getBytes());
  }

  private void serveStaticFile(HttpExchange exchange, String path) throws IOException {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(path.substring(1))) {
      if (is == null) {
        serve404(exchange);
        return;
      }

      byte[] content = is.readAllBytes();
      String contentType = getContentType(path);

      sendResponse(exchange, 200, contentType, content);
    }
  }

  private void sendResponse(
      HttpExchange exchange, int statusCode, String contentType, byte[] content)
      throws IOException {
    exchange.getResponseHeaders().set("Content-Type", contentType);
    exchange.sendResponseHeaders(statusCode, content.length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(content);
    }
  }

  private String getContentType(String path) {
    if (path.endsWith(".css")) return "text/css; charset=UTF-8";
    if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
    throw new IllegalStateException("Unsupported content.");
  }
}
