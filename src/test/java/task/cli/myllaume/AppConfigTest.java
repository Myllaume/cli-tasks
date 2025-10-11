package task.cli.myllaume;

import org.junit.Test;

import task.cli.myllaume.config.AppConfig;
import task.cli.myllaume.config.TaskConfig;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

public class AppConfigTest {

    @Test
    public void testVersion() {

        TaskConfig task1 = new TaskConfig("./work.csv", 0);
        TaskConfig task2 = new TaskConfig("./personal.csv", 1);
        List<TaskConfig> tasks = Arrays.asList(task1, task2);

        AppConfig config = new AppConfig("1.0", tasks);
        assertEquals("1.0", config.getVersion());

    }

    @Test
    public void testTasks() {

        TaskConfig task1 = new TaskConfig("./work.csv", 0);
        TaskConfig task2 = new TaskConfig("./personal.csv", 1);
        List<TaskConfig> tasks = Arrays.asList(task1, task2);

        AppConfig config = new AppConfig("1.0", tasks);
        assertEquals(task1, config.getTasksFiles().get(0));
        assertEquals(task2, config.getTasksFiles().get(1));

    }

}
