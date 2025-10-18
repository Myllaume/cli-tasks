package task.cli.myllaume;

import java.util.List;
import task.cli.myllaume.utils.StringUtils;

/** Génère le HTML pour l'interface web des tâches. */
public class TaskHtmlRenderer {

  private final int port;

  public TaskHtmlRenderer(int port) {
    this.port = port;
  }

  /**
   * Génère la page d'accueil avec la liste des tâches.
   *
   * @param tasks Liste des tâches à afficher
   * @return Le HTML complet de la page
   */
  public String renderHome(List<Task> tasks) {
    StringBuilder listItems = new StringBuilder();
    for (Task task : tasks) {
      String desc = StringUtils.escapeHtml(task.getDescription());
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

  /**
   * Génère la page d'erreur 404.
   *
   * @return Le HTML de la page 404
   */
  public String render404() {
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

  /**
   * Génère la page d'erreur 500.
   *
   * @return Le HTML de la page d'erreur serveur
   */
  public String render500() {
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
}
