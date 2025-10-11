package task.cli.myllaume;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Test;

public class AppTest {

  @Test
  public void testRun() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream oldErr = System.err;
    PrintStream oldOut = System.out;

    try {
      System.setErr(new PrintStream(err)); // setup
      System.setOut(new PrintStream(out));

      App cmd = new App();
      cmd.run();
    } finally {
      System.setErr(oldErr);
      System.setOut(oldOut);
    }

    assertEquals("", out.toString());
    assertEquals("", err.toString());
  }
}
