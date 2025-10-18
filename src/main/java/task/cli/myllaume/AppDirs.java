package task.cli.myllaume;

import java.io.File;
import task.cli.myllaume.utils.Validators;

public class AppDirs {
  private final String configDir;
  private final String cacheDir;
  private final String dataDir;
  private final String appName = "myllaume-tasks";

  public AppDirs() {
    String homeDir = System.getProperty("user.home");
    String osName = System.getProperty("os.name").toLowerCase();

    if (osName.contains("win")) {
      this.configDir = System.getenv("APPDATA") + "/" + appName;
      this.cacheDir = System.getenv("LOCALAPPDATA") + "/" + appName;
      this.dataDir = System.getenv("LOCALAPPDATA") + "/" + appName;
    } else if (osName.contains("mac")) {
      this.configDir = homeDir + "/Library/Application Support" + "/" + appName;
      this.cacheDir = homeDir + "/Library/Caches" + "/" + appName;
      this.dataDir = homeDir + "/Library/Application Support" + "/" + appName;
    } else {
      this.configDir = homeDir + "/.config" + "/" + appName;
      this.cacheDir = homeDir + "/.cache" + "/" + appName;
      this.dataDir = homeDir + "/.local/share" + "/" + appName;
    }

    if (this.configDir == null || this.cacheDir == null || this.dataDir == null) {
      throw new IllegalStateException(
          "Impossible de déterminer les dossiers standards pour l'application.");
    }
  }

  public String getConfigDir() {
    validateDirectory(configDir);
    return configDir;
  }

  public String getCacheDir() {
    validateDirectory(cacheDir);
    return cacheDir;
  }

  public String getDataDir() {
    validateDirectory(dataDir);
    return dataDir;
  }

  private void validateDirectory(String path) {
    Validators.throwNullOrEmptyString(path, "Directory path cannot be null or empty");

    File dir = new File(path);

    if (!dir.exists() && !dir.mkdirs()) {
      throw new IllegalStateException("Unable to create data directory: " + path);
    }

    if (!dir.isDirectory()) {
      throw new IllegalStateException("Path exists but is not a directory: " + path);
    }

    if (!dir.canWrite()) {
      throw new IllegalStateException("No write permission in directory: " + path);
    }
  }
}
