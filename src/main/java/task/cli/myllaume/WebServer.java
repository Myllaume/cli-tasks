package task.cli.myllaume;

import java.io.IOException;

/** Interface pour un serveur web. */
public interface WebServer {

  /**
   * Démarre le serveur web.
   *
   * @throws IOException Si le serveur ne peut pas démarrer
   */
  void start() throws IOException;

  /**
   * Arrête le serveur web.
   *
   * @param delaySeconds Délai en secondes avant l'arrêt complet
   */
  void stop(int delaySeconds);

  /**
   * Retourne l'URL du serveur.
   *
   * @return L'URL complète du serveur
   */
  String getUrl();
}
