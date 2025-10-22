package task.cli.myllaume;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import task.cli.myllaume.db.TaskManager;

@Command(name = "gui", description = "Use graphical interface")
public class CommandGui implements Runnable {

  private final TaskRepositorySqlite repository;
  private final TaskWebServerFactory serverFactory;
  private final BrowserLauncher browserLauncher;
  private final TaskManager taskManager;

  public CommandGui(
      TaskRepositorySqlite repository,
      TaskWebServerFactory serverFactory,
      TaskManager taskManager,
      BrowserLauncher browserLauncher) {
    this.repository = repository;
    this.serverFactory = serverFactory;
    this.taskManager = taskManager;
    this.browserLauncher = browserLauncher;
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
      WebServer server = serverFactory.create(port, repository, taskManager);
      server.start();

      String url = server.getUrl();
      System.out.println("✓ Serveur web démarré sur " + url);
      System.out.println("Appuyez sur Ctrl+C pour arrêter le serveur");

      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    System.out.println("\nArrêt du serveur…");
                    server.stop(0);
                  }));

      if (!noBrowser) {
        browserLauncher.openUrl(url);
      }

      Thread.sleep(Long.MAX_VALUE);

    } catch (Exception e) {
      System.err.println("Erreur lors du démarrage du serveur: " + e.getMessage());
    }
  }
}
