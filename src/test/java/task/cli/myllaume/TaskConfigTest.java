package task.cli.myllaume;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import task.cli.myllaume.config.TaskConfig;

public class TaskConfigTest {

    @Test
    public void testGetFilePath() {
        TaskConfig task = new TaskConfig("test.txt", 0);
        assertEquals("test.txt", task.getFilePath());
    }

    @Test
    public void testGetIndex() {
        TaskConfig task = new TaskConfig("test.txt", 3);
        assertEquals(3, task.getIndex());
    }

}
