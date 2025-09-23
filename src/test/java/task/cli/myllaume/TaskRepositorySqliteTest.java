package task.cli.myllaume;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class TaskRepositorySqliteTest {

    @Test
    public void testInitCreatesDatabaseFile() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);

        repo.init();

        File dbFile = new File(dbPath + "tasks.db");
        assertTrue(dbFile.exists());

    }

    @Test
    public void testInitCreatesDatabaseTable() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);

        repo.init();
        String url = repo.getUrl();

        try (Connection conn = DriverManager.getConnection(url)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='tasks'");
            assertTrue(rs.next());
            assertEquals("tasks", rs.getString("name"));
        }

    }

    @Test
    public void testInitWithNonExistentDirectory() throws Exception {
        String dbPath = "/tmp/nonexistent/path/";
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);

        try {
            repo.init();
            fail("Should have thrown an exception for non-existent directory");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testCreateTaskSuccess() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        Task task = repo.createTask("Test Task", false);

        assertNotNull(task);
        assertEquals("Test Task", task.getDescription());
        assertFalse(task.getCompleted());
        assertTrue(task.getId() > 0);
    }

    @Test
    public void testCreateTaskWithCompletedStatus() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        Task task = repo.createTask("Completed Task", true);

        assertEquals("Completed Task", task.getDescription());
        assertEquals(1, task.getId());
        assertTrue(task.getCompleted());
    }

    @Test
    public void testCreateTaskWithNullName() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        try {
            repo.createTask(null, false);
            fail("Should have thrown IllegalArgumentException for null name");
        } catch (IllegalArgumentException e) {
            assertEquals("Le nom de la tâche ne peut pas être vide.", e.getMessage());
        }
    }

    @Test
    public void testCreateTaskWithEmptyName() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        try {
            repo.createTask("   ", false);
            fail("Should have thrown IllegalArgumentException for empty name");
        } catch (IllegalArgumentException e) {
            assertEquals("Le nom de la tâche ne peut pas être vide.", e.getMessage());
        }
    }

    @Test
    public void testAddMultipleTasks() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        Task task1 = repo.createTask("First Task", false);
        Task task2 = repo.createTask("Second Task", true);

        assertNotEquals(task1.getId(), task2.getId());
        assertEquals("First Task", task1.getDescription());
        assertEquals("Second Task", task2.getDescription());
        assertFalse(task1.getCompleted());
        assertTrue(task2.getCompleted());
    }

    @Test
    public void testRemoveTaskSuccess() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        Task addedTask = repo.createTask("Task to Remove", false);
        assertNotNull(addedTask);
        int taskId = addedTask.getId();

        Task removedTask = repo.removeTask(taskId);

        assertEquals(taskId, removedTask.getId());
        assertEquals("Task to Remove", removedTask.getDescription());
        assertFalse(removedTask.getCompleted());
    }

    @Test
    public void testRemoveNonExistentTask() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        try {
            repo.removeTask(999); // ID qui n'existe pas
            fail("Should have thrown IllegalArgumentException for non-existent task");
        } catch (IllegalArgumentException e) {
            assertEquals("Aucune tâche trouvée avec l'ID: 999", e.getMessage());
        }
    }

    @Test
    public void testGetTasksSuccess() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString() + "/";
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        repo.createTask("Task1", false);
        repo.createTask("Task2", false);
        repo.createTask("Task3", false);
        repo.createTask("Task4", false);

        ArrayList<Task> tasks = repo.getTasks(3);

        assertNotNull(tasks);
        assertEquals(3, tasks.size());
        assertEquals("Task1", tasks.get(0).getDescription());
        assertEquals("Task2", tasks.get(1).getDescription());
        assertEquals("Task3", tasks.get(2).getDescription());
    }

    @Test
    public void testCountImportFromCsv() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString() + "/";
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        repo.importFromCsv("src/test/resources/many.csv");
        int count = repo.countTasks();
        assertEquals(52, count);
    }

    @Test
    public void testSearchTasks() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString() + "/";
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        repo.importFromCsv("src/test/resources/many.csv");
        ArrayList<Task> tasks = repo.searchTasks("test", 5);

        assertEquals(5, tasks.size());
    }

    @Test
    public void testGetTaskSuccess() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString() + "/";
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        Task addedTask = repo.createTask("Test Task", false);
        assertNotNull(addedTask);
        int taskId = addedTask.getId();

        Task task = repo.getTask(taskId);

        assertEquals(taskId, task.getId());
        assertEquals("Test Task", task.getDescription());
        assertFalse(task.getCompleted());
    }

    @Test
    public void testGetNonExistentTask() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString() + "/";
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        try {
            repo.getTask(999); // ID qui n'existe pas
            fail("Should have thrown IllegalArgumentException for non-existent task");
        } catch (IllegalArgumentException e) {
            assertEquals("Aucune tâche trouvée avec l'ID: 999", e.getMessage());
        }
    }

    @Test
    public void testUpdateTaskName() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString() + "/";
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        Task originalTask = repo.createTask("Original Task", false);
        assertNotNull(originalTask);
        int taskId = originalTask.getId();

        Task updatedTask = repo.updateTask(taskId, "Updated Task", null);

        assertNotNull(updatedTask);
        assertEquals(taskId, updatedTask.getId());
        assertEquals("Updated Task", updatedTask.getDescription());
        assertFalse(updatedTask.getCompleted());
    }

    @Test
    public void testUpdateTaskStatus() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString() + "/";
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        Task originalTask = repo.createTask("Task to Complete", false);
        int taskId = originalTask.getId();

        Task updatedTask = repo.updateTask(taskId, null, true);

        assertNotNull(updatedTask);
        assertEquals(taskId, updatedTask.getId());
        assertEquals("Task to Complete", updatedTask.getDescription()); // Nom inchangé
        assertTrue(updatedTask.getCompleted());
    }

    @Test
    public void testUpdateTaskNameAndStatus() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString() + "/";
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        Task originalTask = repo.createTask("Original Task", false);
        int taskId = originalTask.getId();

        Task updatedTask = repo.updateTask(taskId, "Completed Task", true);

        assertNotNull(updatedTask);
        assertEquals(taskId, updatedTask.getId());
        assertEquals("Completed Task", updatedTask.getDescription());
        assertTrue(updatedTask.getCompleted());
    }

    @Test
    public void testUpdateTaskWithEmptyName() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString() + "/";
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        Task originalTask = repo.createTask("Original Task", false);
        int taskId = originalTask.getId();

        try {
            repo.updateTask(taskId, "", true);
            fail("Should have thrown IllegalArgumentException for empty name");
        } catch (IllegalArgumentException e) {
            assertEquals("Le nom de la tâche ne peut pas être vide.", e.getMessage());
        }

    }

    @Test
    public void testUpdateNonExistentTask() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString() + "/";
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        try {
            repo.updateTask(999, "Updated Task", true); // ID qui n'existe pas
            fail("Should have thrown IllegalArgumentException for non-existent task");
        } catch (IllegalArgumentException e) {
            assertEquals("Aucune tâche trouvée avec l'ID: 999", e.getMessage());
        }
    }

}
