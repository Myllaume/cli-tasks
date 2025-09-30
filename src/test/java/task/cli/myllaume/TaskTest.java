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
        Task task = new Task(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW, null, null, null);
        assertEquals("Faire les courses", task.getDescription());
    }

    @Test
    public void testSetDescription() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW, null, null, null);
        task.setDescription("Faire la vaisselle");
        assertEquals("Faire la vaisselle", task.getDescription());
    }

    @Test
    public void testGetId() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW, null, null, null);
        int expected = 1;
        assertEquals(expected, task.getId());
    }

    @Test
    public void testGetCompleted() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW, null, null, null);
        boolean expected = false;
        assertEquals(expected, task.getCompleted());
    }

    @Test
    public void testSetCompleted() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW, null, null, null);
        task.setCompleted(true);
        boolean expected = true;
        assertEquals(expected, task.getCompleted());
    }

    @Test
    public void testGetFulltext() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW, null, null, null);
        String expected = "fairelescourses";
        assertEquals(expected, task.getFulltext());
    }

    @Test
    public void testSetFulltext() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW, null, null, null);
        task.setFulltext("nouveaufulltext");
        String expected = "nouveaufulltext";
        assertEquals(expected, task.getFulltext());
    }

    @Test
    public void testToStringNotCompleted() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW, null, null, null);
        assertEquals("[ ] Faire les courses", task.toString());
    }

    @Test
    public void testToStringCompleted() {
        Task task = new Task(1, "Faire les courses", true, "fairelescourses", TaskPriority.LOW, null, null, null);
        assertEquals("[✓] Faire les courses", task.toString());
    }

    @Test
    public void testToCsvNotCompleted() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW, null, null, null);
        assertEquals("Faire les courses,false", task.toCsv());
    }

    @Test
    public void testToCsvCompleted() {
        Task task = new Task(1, "Faire les courses", true, "fairelescourses", TaskPriority.LOW, null, null, null);
        assertEquals("Faire les courses,true", task.toCsv());
    }

    @Test
    public void testSubTask() {
        ArrayList<Task> subTask = new ArrayList<>();
        subTask.add(new Task(2, "Faire la vaisselle", false, "fairelavaiselle", TaskPriority.LOW, null, null, null));
        Task task = new Task(1, "Faire les courses", true, "fairelescourses", TaskPriority.LOW, null, null, subTask);
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

        Task originalTask = repo.createTask("Tâche de test SQL", true, TaskPriority.LOW, dueDate);

        String url = repo.getUrl();
        String sql = "SELECT id, name, completed, fulltext, priority, created_at, due_at FROM tasks WHERE id = ?";

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
            }
        }
    }
}
