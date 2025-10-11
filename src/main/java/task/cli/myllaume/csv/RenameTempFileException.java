package task.cli.myllaume.csv;

import java.io.IOException;

public class RenameTempFileException extends IOException {
  private final String filePath;

  public RenameTempFileException(String filePath) {
    super("Erreur lors du renommage du fichier temporaire : " + filePath);
    this.filePath = filePath;
  }

  public String getFilePath() {
    return filePath;
  }
}
