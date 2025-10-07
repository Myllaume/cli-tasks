package task.cli.myllaume;

import org.junit.Test;
import static org.junit.Assert.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TaskTest {
    @Test
    public void testGetDescription() {
        Task task = Task.of(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW, Instant.now(), null,
                null, null);
        assertEquals("Faire les courses", task.getDescription());
    }

    @Test
    public void testGetId() {
        Task task = Task.of(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW, Instant.now(), null,
                null, null);
        int expected = 1;
        assertEquals(expected, task.getId());
    }

    @Test
    public void testGetCompleted() {
        Task task = Task.of(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW, Instant.now(), null,
                null, null);
        boolean expected = false;
        assertEquals(expected, task.getCompleted());
    }

    @Test
    public void testGetFulltext() {
        Task task = Task.of(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW, Instant.now(), null,
                null, null);
        String expected = "fairelescourses";
        assertEquals(expected, task.getFulltext());
    }

    @Test
    public void testToStringNotCompleted() {
        Task task = Task.of(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW, Instant.now(), null,
                null,
                null);
        assertEquals("[ ] Faire les courses", task.toString());
    }

    @Test
    public void testToStringCompleted() {
        Task task = Task.of(1, "Faire les courses", true, "fairelescourses", TaskPriority.LOW, Instant.now(), null,
                null,
                null);
        assertEquals("[✓] Faire les courses", task.toString());
    }

    @Test
    public void testToCsvNotCompleted() {
        Task task = Task.of(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW, Instant.now(), null,
                null, null);
        assertEquals("Faire les courses,false", task.toCsv());
    }

    @Test
    public void testToCsvCompleted() {
        Task task = Task.of(1, "Faire les courses", true, "fairelescourses", TaskPriority.LOW, Instant.now(), null,
                null, null);
        assertEquals("Faire les courses,true", task.toCsv());
    }

    @Test
    public void testGetDoneAt() {
        Instant doneAt = Instant.now();

        Task task = Task.of(1, "Faire les courses", true, "fairelescourses", TaskPriority.LOW, Instant.now(), null,
                doneAt, null);
        assertEquals(doneAt, task.getDoneAt());
    }

    @Test
    public void testSubTask() {
        ArrayList<Task> subTask = new ArrayList<>();
        subTask.add(
                Task.of(2, "Faire la vaisselle", false, "fairelavaiselle", TaskPriority.LOW, Instant.now(), null, null,
                        null));
        Task task = Task.of(1, "Faire les courses", true, "fairelescourses", TaskPriority.LOW, Instant.now(), null,
                null, subTask);
        assertEquals(subTask, task.getSubTasks());
    }

    @Test
    public void testFromSqlResult() throws Exception {
        Path tempDir = Files.createTempDirectory("test_fromSqlResult");
        tempDir.toFile().deleteOnExit();

        TaskRepositorySqlite repo = new TaskRepositorySqlite(tempDir.toString());
        repo.init();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse("2025-10-01 15:30", formatter);
        Instant dueDate = localDateTime.atZone(ZoneId.of("Europe/Paris")).toInstant();

        TaskData taskData = TaskData.of("Tâche de test SQL", true, TaskPriority.LOW, Instant.now(), dueDate, null);
        Task originalTask = repo.createTask(taskData);

        String url = repo.getUrl();
        String sql = "SELECT id, name, completed, fulltext, priority, created_at, due_at, done_at FROM tasks WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, originalTask.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next());

                Task taskFromSql = Task.fromSqlResult(rs);

                assertNotNull(taskFromSql);
                assertEquals(originalTask.getId(), taskFromSql.getId());
                assertEquals(originalTask.getDescription(), taskFromSql.getDescription());
                assertEquals(originalTask.getCompleted(), taskFromSql.getCompleted());
                assertEquals(originalTask.getFulltext(), taskFromSql.getFulltext());
                assertEquals(originalTask.getPriority(), taskFromSql.getPriority());
                assertEquals(originalTask.getCreatedAt(), taskFromSql.getCreatedAt());
                assertEquals(originalTask.getDueDate(), taskFromSql.getDueDate());
                assertEquals(originalTask.getDoneAt(), taskFromSql.getDoneAt());
            }
        }
    }

    @Test
    public void testValidateIdPositive() {
        Task task = Task.of(1, "Test description", false, "testfulltext", TaskPriority.LOW, Instant.now(), null, null,
                null);
        assertNotNull(task);
        assertEquals(1, task.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateIdZero() {
        Task.of(0, "Test description", false, "testfulltext", TaskPriority.LOW, Instant.now(), null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateIdNegative() {
        Task.of(-1, "Test description", false, "testfulltext", TaskPriority.LOW, Instant.now(), null, null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testValidateDescriptionNull() {
        Task.of(1, null, false, "testfulltext", TaskPriority.LOW, Instant.now(), null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateDescriptionEmpty() {
        Task.of(1, "", false, "testfulltext", TaskPriority.LOW, Instant.now(), null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateDescriptionWhitespace() {
        Task.of(1, "   ", false, "testfulltext", TaskPriority.LOW, Instant.now(), null, null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testValidateFulltextNull() {
        Task.of(1, "Test description", false, null, TaskPriority.LOW, Instant.now(), null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateFulltextEmpty() {
        Task.of(1, "Test description", false, "", TaskPriority.LOW, Instant.now(), null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateFulltextWhitespace() {
        Task.of(1, "Test description", false, "   ", TaskPriority.LOW, Instant.now(), null, null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testValidatePriorityNull() {
        Task.of(1, "Test description", false, "testfulltext", null, Instant.now(), null, null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testValidateCreatedAtNull() {
        Task.of(1, "Test description", false, "testfulltext", TaskPriority.LOW, null, null, null, null);
    }
}
