package task.cli.myllaume;

import static org.junit.Assert.*;

import org.junit.Test;
import task.cli.myllaume.csv.CsvError;

public class CsvErrorTest {
  @Test
  public void testGetLineNumber() {
    CsvError error = new CsvError(1, "Erreur de format");
    assertEquals(1, error.getLineNumber());
  }

  @Test
  public void testGetMessage() {
    CsvError error = new CsvError(1, "Erreur de format");
    assertEquals("Erreur de format", error.getMessage());
  }

  @Test
  public void testToString() {
    CsvError error = new CsvError(1, "Erreur de format");
    assertEquals("Ligne 1 : Erreur de format", error.toString());
  }
}
