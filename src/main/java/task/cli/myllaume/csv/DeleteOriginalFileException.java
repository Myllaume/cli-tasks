package task.cli.myllaume.csv;

import java.io.IOException;

public class DeleteOriginalFileException extends IOException {
  private final String filePath;

  public DeleteOriginalFileException(String filePath) {
    super("Erreur lors de la suppression du fichier d'origine : " + filePath);
    this.filePath = filePath;
  }

  public String getFilePath() {
    return filePath;
  }
}
