package task.cli.myllaume;

import org.junit.Test;

import task.cli.myllaume.config.AppConfigRepository;
import task.cli.myllaume.config.TaskConfig;

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
        configRepo.addTaskConfig(new TaskConfig("work.csv", -1));
        configRepo.init();

        TaskConfig workTaskConfig = configRepo.getTaskConfig("work.csv");
        assertEquals("work.csv", workTaskConfig.getFilePath());
        assertEquals(-1, workTaskConfig.getIndex());

    }

    @Test
    public void testAddTaskConfig() throws Exception {

        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        AppConfigRepository configRepo = new AppConfigRepository(tempDir.toString());
        configRepo.init();
        configRepo.addTaskConfig(new TaskConfig("work.csv", -1));
        configRepo.addTaskConfig(new TaskConfig("personal.csv", 359));

        TaskConfig workTaskConfig = configRepo.getTaskConfig("work.csv");
        assertEquals("work.csv", workTaskConfig.getFilePath());
        assertEquals(-1, workTaskConfig.getIndex());

        TaskConfig personalTaskConfig = configRepo.getTaskConfig("personal.csv");
        assertEquals("personal.csv", personalTaskConfig.getFilePath());
        assertEquals(359, personalTaskConfig.getIndex());
    }

    @Test
    public void testGetUpdateTaskConfig() throws Exception {

        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        AppConfigRepository configRepo = new AppConfigRepository(tempDir.toString());
        configRepo.init();
        configRepo.addTaskConfig(new TaskConfig("work.csv", -1));
        configRepo.addTaskConfig(new TaskConfig("personal.csv", 359));

        TaskConfig personalTaskConfig = configRepo.getTaskConfig("personal.csv");
        assertEquals("personal.csv", personalTaskConfig.getFilePath());
        assertEquals(359, personalTaskConfig.getIndex());
    }

    @Test
    public void testUpdateTaskConfig() throws Exception {

        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        AppConfigRepository configRepo = new AppConfigRepository(tempDir.toString());
        configRepo.init();
        configRepo.addTaskConfig(new TaskConfig("work.csv", -1));
        configRepo.addTaskConfig(new TaskConfig("personal.csv", 359));
        configRepo.updateTaskConfig(new TaskConfig("personal.csv", 360));
        // AppConfig config = configRepo.read();

        TaskConfig workTaskConfig = configRepo.getTaskConfig("work.csv");
        assertEquals("work.csv", workTaskConfig.getFilePath());
        assertEquals(-1, workTaskConfig.getIndex());

        TaskConfig personalTaskConfig = configRepo.getTaskConfig("personal.csv");
        assertEquals("personal.csv", personalTaskConfig.getFilePath());
        assertEquals(360, personalTaskConfig.getIndex());
    }

    @Test
    public void testRemoveTaskConfig() throws Exception {

        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        AppConfigRepository configRepo = new AppConfigRepository(tempDir.toString());
        configRepo.init();
        configRepo.addTaskConfig(new TaskConfig("work.csv", -1));
        configRepo.addTaskConfig(new TaskConfig("personal.csv", 359));
        configRepo.removeTaskConfig("personal.csv");

        TaskConfig workTaskConfig = configRepo.getTaskConfig("work.csv");
        assertEquals("work.csv", workTaskConfig.getFilePath());
        assertEquals(-1, workTaskConfig.getIndex());

        TaskConfig personalTaskConfig = configRepo.getTaskConfig("personal.csv");
        assertEquals(null, personalTaskConfig);

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
