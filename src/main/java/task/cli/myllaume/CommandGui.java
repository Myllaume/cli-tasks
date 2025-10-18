package task.cli.myllaume;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Set;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import task.cli.myllaume.utils.StringUtils;

@Command(name = "gui", description = "Use graphical interface")
public class CommandGui implements Runnable {

  private final TaskRepositorySqlite repo;

  public CommandGui(TaskRepositorySqlite repo) {
    this.repo = repo;
  }

  @CommandLine.Option(
      names = {"-p", "--port"},
      description = "Port du serveur web (défaut: 8080)",
      defaultValue = "8080")
  private int port;

  @CommandLine.Option(
      names = {"--no-browser"},
      description = "Ne pas ouvrir le navigateur automatiquement")
  private boolean noBrowser;

  @Override
  public void run() {
    try {
      HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
      server.createContext("/", this::handleRequest);
      server.start();

      String url = "http://localhost:" + port;
      System.out.println("✓ Serveur web démarré sur " + url);
      System.out.println("Appuyez sur Ctrl+C pour arrêter le serveur");

      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    System.out.println("\nArrêt du serveur…");
                    server.stop(0);
                  }));

      if (!noBrowser && Desktop.isDesktopSupported()) {
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
          desktop.browse(new URI(url));
        }
      }

      Thread.sleep(Long.MAX_VALUE);

    } catch (Exception e) {
      System.err.println("Erreur lors du démarrage du serveur: " + e.getMessage());
    }
  }

  private void handleRequest(HttpExchange exchange) throws IOException {
    String path = exchange.getRequestURI().getPath();

    try {
      if (Set.of("/styles.css", "/script.js").contains(path)) {
        serveStaticFile(exchange, path);
        return;
      }

      if (path.equals("/")) {
        String response = htmlHome();
        sendResponse(exchange, 200, "text/html; charset=UTF-8", response.getBytes());
        return;
      }

      String response = html404();
      sendResponse(exchange, 404, "text/html; charset=UTF-8", response.getBytes());
    } catch (Exception e) {
      String response = htmlError();
      sendResponse(exchange, 500, "text/html; charset=UTF-8", response.getBytes());
    }
  }

  private String htmlHome() throws Exception {
    ArrayList<Task> tasks = repo.getTasks(100);

    StringBuilder listItems = new StringBuilder();
    for (Task item : tasks) {
      String desc = StringUtils.escapeHtml(item.getDescription());
      listItems.append("<li>").append(desc).append("</li>");
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <title>Tasks</title>
          <link rel="stylesheet" href="styles.css">
        </head>
        <body>
          <div class="container">
            <h1>Hello World!</h1>
            <p>Serveur lancé sur le port %s</p>
            <ul>%s</ul>
          </div>
        </body>
        </html>
        """
        .formatted(port, listItems);
  }

  private String htmlError() {
    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <title>Erreur</title>
          <link rel="stylesheet" href="styles.css">
        </head>
        <body>
          <h1>500 - Erreur serveur</h1>
          <p>Une erreur s'est produite lors du traitement de votre requête.</p>
        </body>
        </html>
        """;
  }

  private String html404() {
    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <title>404 Not Found</title>
          <link rel="stylesheet" href="styles.css">
        </head>
        <body>
          <h1>404 - Page non trouvée</h1>
        </body>
        </html>
        """;
  }

  private void serveStaticFile(HttpExchange exchange, String path) throws IOException {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(path.substring(1))) {
      if (is == null) {
        sendResponse(exchange, 404, "text/html; charset=UTF-8", html404().getBytes());
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
