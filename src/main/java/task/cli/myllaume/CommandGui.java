package task.cli.myllaume;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import picocli.CommandLine;
import picocli.CommandLine.Command;

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

      server.createContext(
          "/",
          new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
              String path = exchange.getRequestURI().getPath();

               if (path.equals("/styles.css")) {
                  serveStaticFile(exchange, path);
                }

              String response;

              try {
                if (path.equals("/")) {
                  response = htmlHome();
                } else {
                  response = html404();
                }
              } catch (Exception e) {
                response = htmlError();
              }

              exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
              exchange.sendResponseHeaders(200, response.getBytes().length);

              try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
              }
            }
          });

      server.start();

      String url = "http://localhost:" + port;
      System.out.println("✓ Serveur web démarré sur " + url);
      System.out.println("Appuyez sur Ctrl+C pour arrêter le serveur");

      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    System.out.println("\nArrêt du serveur...");
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

  private String htmlHome() throws Exception {
    ArrayList<Task> tasks = repo.getTasks(100);

    StringBuilder listItems = new StringBuilder();
    for (Task item : tasks) {
      listItems.append("<li>").append((item.getDescription())).append("</li>");
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <title>Tasks</title>
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

  private String html404() {
    return """
        <!DOCTYPE html>
        <html>
        <head><title>404 Not Found</title></head>
        <body><h1>404 - Page non trouvée</h1></body>
        </html>
        """;
  }

  private String htmlError() {
    return """
        <!DOCTYPE html>
        <html>
        <head><title>Error</title></head>
        <body><h1>500 - Serveur error</h1></body>
        </html>
        """;
  }

  private void serveStaticFile(HttpExchange exchange, String path) throws IOException {
    // // Sécurité : valider et normaliser le chemin
    // if (!path.startsWith("/static/")) {
    //     serve404(exchange);
    //     return;
    // }

    // // Enlever le préfixe /static/ et normaliser
    // String relativePath = path.substring("/static/".length());

    // // Bloquer les tentatives de path traversal
    // if (relativePath.contains("..") || relativePath.contains("//") ||
    // relativePath.startsWith("/")) {
    //     serve404(exchange);
    //     return;
    // }

    // // Construire le chemin complet dans les resources
    // String resourcePath = "static/" + relativePath;

    try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
      if (is == null) {
        serve404(exchange);
        return;
      }

      byte[] content = is.readAllBytes();
      String contentType = getContentType(path);

      sendResponse(exchange, 200, contentType, content);
    }
  }

  private String getContentType(String path) {
    if (path.endsWith(".css")) return "text/css; charset=UTF-8";
    if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
    if (path.endsWith(".html")) return "text/html; charset=UTF-8";
    // if (path.endsWith(".json")) return "application/json; charset=UTF-8";
    // if (path.endsWith(".png")) return "image/png";
    // if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
    // if (path.endsWith(".svg")) return "image/svg+xml";
    // if (path.endsWith(".ico")) return "image/x-icon";
    throw new IllegalStateException("Unsupported content.");
  }
}
