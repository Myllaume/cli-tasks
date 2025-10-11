package task.cli.myllaume;

import static org.junit.Assert.*;

import org.junit.Test;
import task.cli.myllaume.config.AppConfig;

public class AppConfigTest {

  @Test
  public void testVersion() {
    AppConfig config = new AppConfig("1.0");
    assertEquals("1.0", config.getVersion());
  }
}
