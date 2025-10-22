package task.cli.myllaume;

import task.cli.myllaume.db.TaskManager;

public class TaskWebServerFactory {
  public WebServer create(int port, TaskRepositorySqlite repository, TaskManager taskManager) {
    TaskHtmlRenderer renderer = new TaskHtmlRenderer(port);
    return new TaskWebServer(port, repository, renderer, taskManager);
  }
}
