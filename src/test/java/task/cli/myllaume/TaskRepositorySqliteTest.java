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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TaskRepositorySqliteTest {

    @Test
    public void testInitCreatesDatabaseFile() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);

        repo.init();

        File dbFile = new File(dbPath + System.getProperty("file.separator") + "tasks.db");
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
    public void testConstructorWithNullPath() {
        try {
            new TaskRepositorySqlite(null);
            fail("Should have thrown IllegalArgumentException for null path");
        } catch (IllegalArgumentException e) {
            assertEquals("Le chemin de la base de données ne peut pas être nul ou vide.", e.getMessage());
        }
    }

    @Test
    public void testConstructorWithEmptyPath() {
        try {
            new TaskRepositorySqlite("   ");
            fail("Should have thrown IllegalArgumentException for empty path");
        } catch (IllegalArgumentException e) {
            assertEquals("Le chemin de la base de données ne peut pas être nul ou vide.", e.getMessage());
        }
    }

    @Test
    public void testConstructorNormalizesPath() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPathWithoutSlash = tempDir.toString();
        String dbPathWithSlash = tempDir.toString() + "/";

        TaskRepositorySqlite repo1 = new TaskRepositorySqlite(dbPathWithoutSlash);
        TaskRepositorySqlite repo2 = new TaskRepositorySqlite(dbPathWithSlash);

        assertEquals(repo1.getUrl(), repo2.getUrl());
        assertTrue(repo1.getUrl().endsWith("tasks.db"));
    }

    @Test
    public void testCreateTaskSuccess() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse("2025-10-01 15:30", formatter);
        Instant dueDate = localDateTime.atZone(ZoneId.of("Europe/Paris")).toInstant();

        Task task = repo.createTask("Test Task", false, TaskPriority.LOW, dueDate);

        assertNotNull(task);
        assertEquals("Test Task", task.getDescription());
        assertFalse(task.getCompleted());
        assertEquals(TaskPriority.LOW, task.getPriority());
        assertTrue(task.getId() > 0);
        assertEquals(dueDate, task.getDueDate());
    }

    @Test
    public void testCreateTaskWithNullName() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        try {
            repo.createTask(null, false, TaskPriority.LOW, null);
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
            repo.createTask("   ", false, TaskPriority.LOW, null);
            fail("Should have thrown IllegalArgumentException for empty name");
        } catch (IllegalArgumentException e) {
            assertEquals("Le nom de la tâche ne peut pas être vide.", e.getMessage());
        }
    }

    @Test
    public void testRemoveTaskSuccess() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        Task addedTask = repo.createTask("Task to Remove", false, TaskPriority.LOW, null);
        assertNotNull(addedTask);
        int taskId = addedTask.getId();

        Task removedTask = repo.removeTask(taskId);

        assertEquals(addedTask.getId(), removedTask.getId());
        assertEquals(addedTask.getDescription(), removedTask.getDescription());
        assertEquals(addedTask.getCompleted(), removedTask.getCompleted());
        assertEquals(addedTask.getFulltext(), removedTask.getFulltext());
        assertEquals(addedTask.getPriority(), removedTask.getPriority());
        assertNull(repo.getTask(taskId));
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

        Task task = repo.createTask("Task1", false, TaskPriority.LOW, null);
        repo.createTask("Task2", false, TaskPriority.MEDIUM, null);
        repo.createTask("Task3", false, TaskPriority.LOW, null);
        repo.createTask("Task4", false, TaskPriority.HIGH, null);

        ArrayList<Task> tasks = repo.getTasks(3);

        assertNotNull(tasks);
        assertEquals(3, tasks.size());
        assertEquals(task.getId(), tasks.get(0).getId());
        assertEquals(task.getDescription(), tasks.get(0).getDescription());
        assertEquals(task.getCompleted(), tasks.get(0).getCompleted());
        assertEquals(task.getFulltext(), tasks.get(0).getFulltext());
        assertEquals(task.getPriority(), tasks.get(0).getPriority());
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
    public void testSearchTasksDone() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString() + "/";
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        repo.importFromCsv("src/test/resources/many.csv");
        ArrayList<Task> tasks = repo.searchTasksDone("test", 100);

        assertEquals(8, tasks.size());
    }

    @Test
    public void testSearchTasksTodo() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString() + "/";
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        repo.importFromCsv("src/test/resources/many.csv");
        ArrayList<Task> tasks = repo.searchTasksTodo("test", 100);

        assertEquals(1, tasks.size());
    }

    @Test
    public void testGetTaskSuccess() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString() + "/";
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        Task addedTask = repo.createTask("Test Task", false, TaskPriority.LOW, null);
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

        assertNull(repo.getTask(999));
    }

    @Test
    public void testUpdateTaskName() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString() + "/";
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        Task originalTask = repo.createTask("Original Task", false, TaskPriority.LOW, null);
        assertNotNull(originalTask);
        int taskId = originalTask.getId();

        Task updatedTask = repo.updateTask(taskId, "Updated Task", null, null);

        assertNotNull(updatedTask);
        assertEquals(originalTask.getId(), updatedTask.getId());
        assertEquals("Updated Task", updatedTask.getDescription());
        assertEquals(originalTask.getCompleted(), updatedTask.getCompleted());
        assertEquals("updatedtask", updatedTask.getFulltext());
        assertEquals(originalTask.getPriority(), updatedTask.getPriority());
    }

    @Test
    public void testUpdateTaskStatus() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString() + "/";
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        Task originalTask = repo.createTask("Task to Complete", false, TaskPriority.LOW, null);
        int taskId = originalTask.getId();

        Task updatedTask = repo.updateTask(taskId, null, true, null);

        assertNotNull(updatedTask);
        assertEquals(originalTask.getId(), updatedTask.getId());
        assertEquals(originalTask.getDescription(), updatedTask.getDescription());
        assertEquals(true, updatedTask.getCompleted());
        assertEquals(originalTask.getFulltext(), updatedTask.getFulltext());
        assertEquals(originalTask.getPriority(), updatedTask.getPriority());
    }

    @Test
    public void testUpdateTaskPriority() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString() + "/";
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        Task originalTask = repo.createTask("Original Task", false, TaskPriority.LOW, null);
        int taskId = originalTask.getId();

        Task updatedTask = repo.updateTask(taskId, null, null, TaskPriority.HIGH);

        assertNotNull(updatedTask);
        assertEquals(originalTask.getId(), updatedTask.getId());
        assertEquals(originalTask.getDescription(), updatedTask.getDescription());
        assertEquals(originalTask.getCompleted(), updatedTask.getCompleted());
        assertEquals(originalTask.getFulltext(), updatedTask.getFulltext());
        assertEquals(TaskPriority.HIGH, updatedTask.getPriority());
    }

    @Test
    public void testUpdateTaskWithEmptyName() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString() + "/";
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        Task originalTask = repo.createTask("Original Task", false, TaskPriority.LOW, null);
        int taskId = originalTask.getId();

        try {
            repo.updateTask(taskId, "", true, null);
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
            repo.updateTask(999, "Updated Task", true, null); // ID qui n'existe pas
            fail("Should have thrown IllegalArgumentException for non-existent task");
        } catch (IllegalArgumentException e) {
            assertEquals("Aucune tâche trouvée avec l'ID: 999", e.getMessage());
        }
    }

    @Test
    public void testAddSubTask() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        TaskRepositorySqlite repo = new TaskRepositorySqlite(tempDir.toString());
        repo.init();

        Task parentTask = repo.createTask("Parent Task", false, TaskPriority.MEDIUM, null);
        assertNotNull(parentTask);

        Task subTask = repo.createSubTask(parentTask.getId(), "Sub Task", false, TaskPriority.LOW);
        repo.createSubTask(parentTask.getId(), "Sub Task", false, TaskPriority.LOW);
        repo.createSubTask(parentTask.getId(), "Sub Task", false, TaskPriority.LOW);

        Task taskFromDb = repo.getTaskWithSubTasks(parentTask.getId(), 2);
        assertNotNull(taskFromDb);
        assertEquals(2, taskFromDb.getSubTasks().size());
        assertEquals(subTask.getId(), taskFromDb.getSubTasks().get(0).getId());
    }

    @Test
    public void testFailAddSubTask() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        TaskRepositorySqlite repo = new TaskRepositorySqlite(tempDir.toString());
        repo.init();

        try {
            repo.createSubTask(0, "Sub Task", false, TaskPriority.LOW);
            fail("Should throw for unknown parent task");
        } catch (UnknownTaskException e) {
            assertEquals("Aucune tâche trouvée avec l'ID: 0", e.getMessage());
        }
    }

    /**
     * 
     * @Test
     *       public void testDeleteTaskDeleteSubTasks() throws Exception {
     *       Path tempDir = Files.createTempDirectory("tests");
     *       tempDir.toFile().deleteOnExit();
     * 
     *       TaskRepositorySqlite repo = new
     *       TaskRepositorySqlite(tempDir.toString());
     *       repo.init();
     * 
     *       Task parentTask = repo.createTask("Parent Task", false,
     *       TaskPriority.MEDIUM, null);
     *       assertNotNull(parentTask);
     * 
     *       Task subTask = repo.createSubTask(parentTask.getId(), "Sub Task",
     *       false, TaskPriority.LOW);
     *       assertNotNull(subTask);
     * 
     *       Task deletedTask = repo.removeTask(parentTask.getId());
     *       assertNotNull(deletedTask);
     * 
     *       Task subTaskFromDb = repo.getTask(subTask.getId());
     *       assertNull(subTaskFromDb);
     *       }
     * 
     */

}
