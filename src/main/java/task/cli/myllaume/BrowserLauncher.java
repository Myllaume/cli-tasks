package task.cli.myllaume;

/** Interface pour ouvrir une URL dans le navigateur par défaut du système. */
public interface BrowserLauncher {

  /**
   * Ouvre une URL dans le navigateur par défaut.
   *
   * @param url L'URL à ouvrir
   */
  void openUrl(String url);
}
