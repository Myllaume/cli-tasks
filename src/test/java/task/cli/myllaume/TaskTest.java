package task.cli.myllaume;

import org.junit.Test;
import static org.junit.Assert.*;

public class TaskTest {
    @Test
    public void testGetDescription() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses");
        assertEquals("Faire les courses", task.getDescription());
    }

    @Test
    public void testSetDescription() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses");
        task.setDescription("Faire la vaisselle");
        assertEquals("Faire la vaisselle", task.getDescription());
    }

    @Test
    public void testGetId() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses");
        int expected = 1;
        assertEquals(expected, task.getId());
    }

    @Test
    public void testGetCompleted() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses");
        boolean expected = false;
        assertEquals(expected, task.getCompleted());
    }

    @Test
    public void testSetCompleted() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses");
        task.setCompleted(true);
        boolean expected = true;
        assertEquals(expected, task.getCompleted());
    }

    @Test
    public void testGetFulltext() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses");
        String expected = "fairelescourses";
        assertEquals(expected, task.getFulltext());
    }

    @Test
    public void testSetFulltext() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses");
        task.setFulltext("nouveaufulltext");
        String expected = "nouveaufulltext";
        assertEquals(expected, task.getFulltext());
    }

    @Test
    public void testToStringNotCompleted() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses");
        assertEquals("[✗] Faire les courses", task.toString());
    }

    @Test
    public void testToStringCompleted() {
        Task task = new Task(1, "Faire les courses", true, "fairelescourses");
        assertEquals("[✓] Faire les courses", task.toString());
    }

    @Test
    public void testToCsvNotCompleted() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses");
        assertEquals("Faire les courses,false", task.toCsv());
    }

    @Test
    public void testToCsvCompleted() {
        Task task = new Task(1, "Faire les courses", true, "fairelescourses");
        assertEquals("Faire les courses,true", task.toCsv());
    }
}
