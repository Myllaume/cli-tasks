package task.cli.myllaume.csv;

public class CsvError {
  private final int lineNumber;
  private final String message;

  public CsvError(int lineNumber, String message) {
    this.lineNumber = lineNumber;
    this.message = message;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "Ligne " + lineNumber + " : " + message;
  }
}
