package task.cli.myllaume.csv;

import java.io.IOException;

public class FileNotExistsException extends IOException {
  private final String filePath;

  public FileNotExistsException(String filePath) {
    super("Le fichier n'existe pas : " + filePath);
    this.filePath = filePath;
  }

  public String getFilePath() {
    return filePath;
  }
}
