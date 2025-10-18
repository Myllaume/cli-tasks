package task.cli.myllaume;

/** Factory pour créer des instances de TaskWebServer. */
public class TaskWebServerFactory implements WebServerFactory {

  @Override
  public WebServer create(int port, TaskRepositorySqlite repository) {
    TaskHtmlRenderer renderer = new TaskHtmlRenderer(port);
    return new TaskWebServer(port, repository, renderer);
  }
}
