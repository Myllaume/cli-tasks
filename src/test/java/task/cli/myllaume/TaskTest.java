package task.cli.myllaume;

import org.junit.Test;
import static org.junit.Assert.*;

public class TaskTest {
    @Test
    public void testGetDescription() {
        Task task = new Task(1, "Faire les courses", false);
        String expected = "Faire les courses";
        assertEquals(expected, task.getDescription());
    }

    @Test
    public void testSetDescription() {
        Task task = new Task(1, "Faire les courses", false);
        task.setDescription("Faire la vaisselle");
        String expected = "Faire la vaisselle";
        assertEquals(expected, task.getDescription());
    }

    @Test
    public void testGetId() {
        Task task = new Task(1, "Faire les courses", false);
        int expected = 1;
        assertEquals(expected, task.getId());
    }

    @Test
    public void testGetCompleted() {
        Task task = new Task(1, "Faire les courses", false);
        boolean expected = false;
        assertEquals(expected, task.getCompleted());
    }

    @Test
    public void testSetCompleted() {
        Task task = new Task(1, "Faire les courses", false);
        task.setCompleted(true);
        boolean expected = true;
        assertEquals(expected, task.getCompleted());
    }

    @Test
    public void testToStringNotCompleted() {
        Task task = new Task(1, "Faire les courses", false);
        String expected = "1 - Faire les courses [✗]";
        assertEquals(expected, task.toString());
    }

    @Test
    public void testToStringCompleted() {
        Task task = new Task(1, "Faire les courses", true);
        String expected = "1 - Faire les courses [✓]";
        assertEquals(expected, task.toString());
    }

    @Test
    public void testToCsvNotCompleted() {
        Task task = new Task(1, "Faire les courses", false);
        String expected = "Faire les courses,false";
        assertEquals(expected, task.toCsv());
    }

    @Test
    public void testToCsvCompleted() {
        Task task = new Task(1, "Faire les courses", true);
        String expected = "Faire les courses,true";
        assertEquals(expected, task.toCsv());
    }
}
