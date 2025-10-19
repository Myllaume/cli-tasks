package task.cli.myllaume;

import java.io.File;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import task.cli.myllaume.db.TaskManager;

@Command(name = "import", description = "Import tasks from file")
public class CommandImport implements Runnable {

  private final TaskManager manager;

  public CommandImport(TaskManager manager) {
    this.manager = manager;
  }

  @Parameters(index = "0", description = "Chemin du fichier CSV")
  String filePath;

  @Override
  public void run() {
    try {
      throwIfFileNotExist();
      throwIfFileIsNotCsv();

      int count = manager.importFromCsvOnCurrentProject(filePath);
      System.out.println(count + " tâches ont été importées.");
    } catch (Exception e) {
      System.out.println("Les tâches n'ont pas pu être importées: " + e.getMessage());
    }
  }

  private void throwIfFileNotExist() throws Exception {
    File file = new File(filePath);
    if (!file.exists()) {
      throw new Exception("File to import does not exist at " + file.getAbsolutePath());
    }
  }

  private void throwIfFileIsNotCsv() throws Exception {
    if (!filePath.endsWith(".csv")) {
      throw new Exception("File to import does must be .csv.");
    }
  }
}
