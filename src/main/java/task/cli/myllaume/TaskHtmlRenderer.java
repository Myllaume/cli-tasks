package task.cli.myllaume;

import java.util.List;
import task.cli.myllaume.utils.StringUtils;

public class TaskHtmlRenderer {

  private final int port;

  public TaskHtmlRenderer(int port) {
    this.port = port;
  }

  public String renderHome(List<Task> tasks) {
    StringBuilder taskLines = new StringBuilder();
    for (Task task : tasks) {
      String desc = StringUtils.escapeHtml(task.getDescription());
      taskLines
          .append("<tr>")
          .append("<td>")
          .append(task.getCompleted() ? "✅" : "⬜️" )
          .append("</td>")
          .append("<td>")
          .append(desc)
          .append("</td>")
          .append("<td>")
          .append(task.getPriority().getLabel())
          .append("</td>")
          .append("</tr>");
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

            <form method="GET" action="/search" >
              <input name="keyword" type="search" />
            </form>

            <table>
              <thead>
                <tr>
                  <th scope="col">Terminé</th>
                  <th scope="col">Description</th>
                  <th scope="col">Priorité</th>
                </tr>
              </thead>
              <tbody>%s</tbody>
            </table>
          </div>
        </body>
        </html>
        """
        .formatted(taskLines);
  }

  public String renderForm() {
    StringBuilder priorityOptions = new StringBuilder();
    for (TaskPriority p : TaskPriority.values()) {
      priorityOptions
          .append("<option ")
          .append("value=")
          .append("\"")
          .append(p.getLevel())
          .append("\"")
          .append(" >")
          .append(p.getLabel())
          .append("</option>");
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
            <form method="POST" action="/add-tasks">
              <label for="description">Description :</label>
              <input type="text" id="description" name="description" required>
              <label for="priority">Priorité :</label>
              <select id="priority" name="priority">%s</select>
              <input type="submit" value="Ajouter la tâche">
            </form>
          </div>
        </body>
        </html>
        """
        .formatted(priorityOptions);
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

  public String render500(String message) {
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
          <pre>%s</pre>
          <a href="/">Retour à la racine</a>
        </body>
        </html>
        """
        .formatted(message);
  }
}
