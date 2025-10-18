package task.cli.myllaume;

/** Factory pour créer des instances de WebServer. */
public interface WebServerFactory {

  /**
   * Crée une nouvelle instance de WebServer.
   *
   * @param port Le port sur lequel le serveur doit écouter
   * @param repository Le repository pour accéder aux tâches
   * @return Une nouvelle instance de WebServer
   */
  WebServer create(int port, TaskRepositorySqlite repository);
}
