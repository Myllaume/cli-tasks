package task.cli.myllaume;

public class AppDirs {
  private final String configDir;
  private final String cacheDir;
  private final String dataDir;

  public AppDirs() {
    String homeDir = System.getProperty("user.home");
    String osName = System.getProperty("os.name").toLowerCase();

    if (osName.contains("win")) {
      this.configDir = System.getenv("APPDATA");
      this.cacheDir = System.getenv("LOCALAPPDATA");
      this.dataDir = System.getenv("LOCALAPPDATA");
    } else if (osName.contains("mac")) {
      this.configDir = homeDir + "/Library/Application Support";
      this.cacheDir = homeDir + "/Library/Caches";
      this.dataDir = homeDir + "/Library/Application Support";
    } else {
      this.configDir = homeDir + "/.config";
      this.cacheDir = homeDir + "/.cache";
      this.dataDir = homeDir + "/.local/share";
    }

    if (this.configDir == null || this.cacheDir == null || this.dataDir == null) {
      throw new IllegalStateException(
          "Impossible de déterminer les dossiers standards pour l'application.");
    }
  }

  public String getConfigDir() {
    return configDir;
  }

  public String getCacheDir() {
    return cacheDir;
  }

  public String getDataDir() {
    return dataDir;
  }
}
