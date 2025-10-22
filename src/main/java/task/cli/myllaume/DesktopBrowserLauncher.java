package task.cli.myllaume;

import java.awt.Desktop;
import java.net.URI;

public class DesktopBrowserLauncher implements BrowserLauncher {

  @Override
  public void openUrl(String url) {
    if (!Desktop.isDesktopSupported()) {
      return;
    }

    Desktop desktop = Desktop.getDesktop();
    if (!desktop.isSupported(Desktop.Action.BROWSE)) {
      return;
    }

    try {
      desktop.browse(new URI(url));
    } catch (Exception e) {
      // Silently fail
    }
  }
}
