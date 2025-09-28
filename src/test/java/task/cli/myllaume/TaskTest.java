package task.cli.myllaume;

import org.junit.Test;
import static org.junit.Assert.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TaskTest {
    @Test
    public void testGetDescription() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW);
        assertEquals("Faire les courses", task.getDescription());
    }

    @Test
    public void testSetDescription() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW);
        task.setDescription("Faire la vaisselle");
        assertEquals("Faire la vaisselle", task.getDescription());
    }

    @Test
    public void testGetId() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW);
        int expected = 1;
        assertEquals(expected, task.getId());
    }

    @Test
    public void testGetCompleted() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW);
        boolean expected = false;
        assertEquals(expected, task.getCompleted());
    }

    @Test
    public void testSetCompleted() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW);
        task.setCompleted(true);
        boolean expected = true;
        assertEquals(expected, task.getCompleted());
    }

    @Test
    public void testGetFulltext() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW);
        String expected = "fairelescourses";
        assertEquals(expected, task.getFulltext());
    }

    @Test
    public void testSetFulltext() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW);
        task.setFulltext("nouveaufulltext");
        String expected = "nouveaufulltext";
        assertEquals(expected, task.getFulltext());
    }

    @Test
    public void testToStringNotCompleted() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW);
        assertEquals("[✗] Faire les courses", task.toString());
    }

    @Test
    public void testToStringCompleted() {
        Task task = new Task(1, "Faire les courses", true, "fairelescourses", TaskPriority.LOW);
        assertEquals("[✓] Faire les courses", task.toString());
    }

    @Test
    public void testToCsvNotCompleted() {
        Task task = new Task(1, "Faire les courses", false, "fairelescourses", TaskPriority.LOW);
        assertEquals("Faire les courses,false", task.toCsv());
    }

    @Test
    public void testToCsvCompleted() {
        Task task = new Task(1, "Faire les courses", true, "fairelescourses", TaskPriority.LOW);
        assertEquals("Faire les courses,true", task.toCsv());
    }

    @Test
    public void testFromSqlResultWithRealDatabase() throws Exception {
        Path tempDir = Files.createTempDirectory("test_fromSqlResult");
        tempDir.toFile().deleteOnExit();

        TaskRepositorySqlite repo = new TaskRepositorySqlite(tempDir.toString());
        repo.init();

        Task originalTask = repo.createTask("Tâche de test SQL", true, TaskPriority.LOW);

        String url = repo.getUrl();
        String sql = "SELECT id, name, completed, fulltext, priority FROM tasks WHERE id = ?";

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
            }
        }
    }

}
