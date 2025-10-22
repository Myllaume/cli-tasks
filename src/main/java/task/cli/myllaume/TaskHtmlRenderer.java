package task.cli.myllaume;

import java.util.List;
import task.cli.myllaume.utils.StringUtils;

public class TaskHtmlRenderer {

  private final int port;

  public TaskHtmlRenderer(int port) {
    this.port = port;
  }

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
            <a href="/form">Ajouter une tâche</a>
            <ul>%s</ul>
          </div>
        </body>
        </html>
        """
        .formatted(listItems);
  }

  public String renderForm() {
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
            <form method="POST" action="/add-tasks">
              <label for="description">Description de la tâche :</label>
              <input type="text" id="description" name="description" required>
              <input type="submit" value="Ajouter la tâche">
            </form>
          </div>
        </body>
        </html>
        """
        .formatted();
  }

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
