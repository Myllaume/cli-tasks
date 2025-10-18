package task.cli.myllaume;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "gui", description = "Use graphical interface")
public class CommandGui implements Runnable {

  public CommandGui() {}

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

              String response;

              if (path.equals("/")) {
                response = htmlHome();
              } else {
                response = html404();
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

  private String htmlHome() {
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
          </div>
        </body>
        </html>
        """
        .formatted(port);
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
}
