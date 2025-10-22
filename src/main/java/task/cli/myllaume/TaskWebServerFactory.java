package task.cli.myllaume;

public class TaskWebServerFactory {
  public WebServer create(int port, TaskRepositorySqlite repository) {
    TaskHtmlRenderer renderer = new TaskHtmlRenderer(port);
    return new TaskWebServer(port, repository, renderer);
  }
}
