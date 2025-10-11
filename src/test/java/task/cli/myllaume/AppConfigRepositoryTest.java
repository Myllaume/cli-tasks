package task.cli.myllaume;

import org.junit.Test;

import task.cli.myllaume.config.AppConfigRepository;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;

public class AppConfigRepositoryTest {

    @Test
    public void testInit() throws Exception {

        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        AppConfigRepository configRepo = new AppConfigRepository(tempDir.toString());
        configRepo.init();
        String appVersion = configRepo.getAppVersion();
        assertEquals("1.0", appVersion);
    }

    @Test
    public void testInitTwiceWithoutOverwrite() throws Exception {

        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        AppConfigRepository configRepo = new AppConfigRepository(tempDir.toString());
        configRepo.init();
        configRepo.setAppVersion("2.0");
        configRepo.init();

        String appVersion = configRepo.getAppVersion();
        assertEquals("2.0", appVersion);
    }

    @Test
    public void testSetAppVersion() throws Exception {

        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        AppConfigRepository configRepo = new AppConfigRepository(tempDir.toString());
        configRepo.init();
        configRepo.setAppVersion("2.0");

        String appVersion = configRepo.getAppVersion();
        assertEquals("2.0", appVersion);
    }

}
