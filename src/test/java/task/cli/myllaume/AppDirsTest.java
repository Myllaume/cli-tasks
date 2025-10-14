package task.cli.myllaume;

import static org.junit.Assert.*;

import org.junit.Ignore;

public class AppDirsTest {

  @Ignore
  public void testHomeDirOnLinux() {
    System.setProperty("user.home", "/home/testuser");
    System.setProperty("os.name", "Linux");

    AppDirs dirs = new AppDirs();
    assertEquals("/home/testuser/.config", dirs.getConfigDir());
    assertEquals("/home/testuser/.cache", dirs.getCacheDir());
    assertEquals("/home/testuser/.local/share", dirs.getDataDir());
  }

  @Ignore
  public void testHomeDirOnMac() {
    System.setProperty("user.home", "/Users/testuser");
    System.setProperty("os.name", "Mac OS X");

    AppDirs dirs = new AppDirs();
    assertEquals("/Users/testuser/Library/Application Support", dirs.getConfigDir());
    assertEquals("/Users/testuser/Library/Caches", dirs.getCacheDir());
    assertEquals("/Users/testuser/Library/Application Support", dirs.getDataDir());
  }
}
