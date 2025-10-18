package task.cli.myllaume;

import java.awt.Desktop;
import java.net.URI;

/** Implémentation de BrowserLauncher utilisant l'API Desktop de Java. */
public class DesktopBrowserLauncher implements BrowserLauncher {

  @Override
  public void openUrl(String url) {
    if (!Desktop.isDesktopSupported()) {
      System.err.println("Desktop non supporté sur ce système");
      return;
    }

    Desktop desktop = Desktop.getDesktop();
    if (!desktop.isSupported(Desktop.Action.BROWSE)) {
      System.err.println("L'ouverture du navigateur n'est pas supportée sur ce système");
      return;
    }

    try {
      desktop.browse(new URI(url));
    } catch (Exception e) {
      System.err.println("Impossible d'ouvrir le navigateur: " + e.getMessage());
    }
  }
}
