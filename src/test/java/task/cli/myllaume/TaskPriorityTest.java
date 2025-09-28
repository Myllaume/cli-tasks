package task.cli.myllaume;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.List;

public class TaskPriorityTest {

    @Test
    public void testGetLevel() {
        assertEquals(1, TaskPriority.LOW.getLevel());
        assertEquals(2, TaskPriority.MEDIUM.getLevel());
        assertEquals(3, TaskPriority.HIGH.getLevel());
        assertEquals(4, TaskPriority.CRITICAL.getLevel());
    }

    @Test
    public void testGetLabel() {
        assertEquals("faible", TaskPriority.LOW.getLabel());
        assertEquals("moyenne", TaskPriority.MEDIUM.getLabel());
        assertEquals("haute", TaskPriority.HIGH.getLabel());
        assertEquals("critique", TaskPriority.CRITICAL.getLabel());
    }

    @Test
    public void testFromLevelValidValues() {
        assertEquals(TaskPriority.LOW, TaskPriority.fromLevel(1));
        assertEquals(TaskPriority.MEDIUM, TaskPriority.fromLevel(2));
        assertEquals(TaskPriority.HIGH, TaskPriority.fromLevel(3));
        assertEquals(TaskPriority.CRITICAL, TaskPriority.fromLevel(4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromLevelInvalidValue() {
        TaskPriority.fromLevel(999);
    }

    @Test
    public void testFromLevelInvalidValueMessage() {
        try {
            TaskPriority.fromLevel(999);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Niveau inconnu : 999", e.getMessage());
        }
    }

    @Test
    public void testEnumValues() {
        TaskPriority[] values = TaskPriority.values();
        assertEquals(4, values.length);

        List<TaskPriority> valuesList = Arrays.asList(values);
        assertTrue(valuesList.contains(TaskPriority.LOW));
        assertTrue(valuesList.contains(TaskPriority.MEDIUM));
        assertTrue(valuesList.contains(TaskPriority.HIGH));
        assertTrue(valuesList.contains(TaskPriority.CRITICAL));
    }

    @Test
    public void testValueOf() {
        assertEquals(TaskPriority.LOW, TaskPriority.valueOf("LOW"));
        assertEquals(TaskPriority.MEDIUM, TaskPriority.valueOf("MEDIUM"));
        assertEquals(TaskPriority.HIGH, TaskPriority.valueOf("HIGH"));
        assertEquals(TaskPriority.CRITICAL, TaskPriority.valueOf("CRITICAL"));
    }

}
