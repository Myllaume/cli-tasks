package task.cli.myllaume;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.nio.file.Path;

public class TaskRepositoryTest {
    @Test
    public void testReadValidLines() {
        TaskRepository repo = new TaskRepository("src/test/resources/valid-tasks.csv");
        ArrayList<Task> tasks = repo.getTasks();

        assertEquals(2, tasks.size());
    }

    @Test
    public void testReadWithInvalidLines() {
        TaskRepository repo = new TaskRepository("src/test/resources/invalid-tasks.csv");

        ArrayList<Task> tasks = repo.getTasks();
        assertEquals(1, tasks.size());

        ArrayList<CsvError> errors = repo.getErrors();
        assertEquals(1, errors.size());
        assertEquals(3, errors.get(0).getLineNumber());
        assertEquals("Ligne mal formée ou erreur de conversion", errors.get(0).getMessage());
    }

    @Test
    public void testReadWithInvalidHeader() {
        TaskRepository repo = new TaskRepository("src/test/resources/invalid-header-tasks.csv");

        ArrayList<Task> tasks = repo.getTasks();
        assertEquals(0, tasks.size());

        ArrayList<CsvError> errors = repo.getErrors();
        assertEquals(1, errors.size());
        assertEquals(1, errors.get(0).getLineNumber());
        assertEquals("Format d'en-tête incorrect.", errors.get(0).getMessage());
    }

    @Test()
    public void testReadWithUnknownFile() {
        TaskRepository repo = new TaskRepository("src/test/resources/unknown.csv");

        ArrayList<Task> tasks = repo.getTasks();
        assertEquals(0, tasks.size());
    }

    @Test()
    public void testAddLineAtEnd() throws IOException {
        File tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("description,completed");
            writer.newLine();
        }

        TaskRepository repo = new TaskRepository(tempFile.getAbsolutePath());
        Task task = new Task(1, "Test tâche", false);

        repo.addLineAtEnd(task);

        ArrayList<Task> tasks = repo.getTasks();
        assertEquals(1, tasks.size());
        assertEquals("Test tâche", tasks.get(0).getDescription());
        assertFalse(tasks.get(0).getCompleted());
    }

    @Test()
    public void testRemoveLine() throws IOException {
        File tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit();

        Files.copy(
                Paths.get("src/test/resources/valid-tasks.csv"),
                tempFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        TaskRepository repo = new TaskRepository(tempFile.getAbsolutePath());
        ArrayList<Task> tasks = repo.getTasks();
        assertEquals(2, tasks.size());

        repo.removeLine(1);
        repo.removeLine(1);

        TaskRepository repoAfter = new TaskRepository(tempFile.getAbsolutePath());
        ArrayList<Task> tasksAfterRemove = repoAfter.getTasks();
        assertEquals(0, repoAfter.getErrors().size());
        assertEquals(0, tasksAfterRemove.size());
    }

    @Test()
    public void testRemoveLineFail() throws IOException {
        File tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit();

        Files.copy(
                Paths.get("src/test/resources/valid-tasks.csv"),
                tempFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        TaskRepository repo = new TaskRepository(tempFile.getAbsolutePath());
        ArrayList<Task> tasks = repo.getTasks();
        assertEquals(2, tasks.size());

        try {
            repo.removeLine(99);
            fail("Should throw IOException.");
        } catch (IOException e) {
            assertEquals("Line number 99 does not exist.", e.getMessage());
        }

        TaskRepository repoAfter = new TaskRepository(tempFile.getAbsolutePath());
        ArrayList<Task> tasksAfterRemove = repoAfter.getTasks();
        assertEquals(2, tasksAfterRemove.size());
    }

    @Test()
    public void testUpdateLine() throws IOException {
        File tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit();

        Files.copy(
                Paths.get("src/test/resources/valid-tasks.csv"),
                tempFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        TaskRepository repo = new TaskRepository(tempFile.getAbsolutePath());
        ArrayList<Task> tasks = repo.getTasks();
        assertEquals(2, tasks.size());
        assertEquals("Faire la vaisselle", tasks.get(1).getDescription());

        repo.updateLine(2, "Updated task", true);

        TaskRepository repoAfter = new TaskRepository(tempFile.getAbsolutePath());
        ArrayList<Task> tasksAfterUpdate = repoAfter.getTasks();
        assertEquals(2, tasksAfterUpdate.size());
        assertEquals("Updated task", tasksAfterUpdate.get(1).getDescription());
    }

    @Test()
    public void testInit() throws IOException {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        TaskRepository repo = new TaskRepository(tempDir.toString() + "/tasks.csv");
        repo.init(false);

        ArrayList<Task> tasks = repo.getTasks();
        assertEquals(0, tasks.size());
        assertEquals(0, repo.getErrors().size());

        repo.addLineAtEnd(new Task(1, "Test", false));
        ArrayList<Task> tasksAfterAdd = repo.getTasks();
        assertEquals(1, tasksAfterAdd.size());
    }

    @Test()
    public void testInitWithOverwrite() throws IOException {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        TaskRepository repo = new TaskRepository(tempDir.toString() + "/tasks.csv");
        repo.init(false);

        ArrayList<Task> tasks = repo.getTasks();
        assertEquals(0, tasks.size());
        assertEquals(0, repo.getErrors().size());

        repo.addLineAtEnd(new Task(1, "Test", false));
        ArrayList<Task> tasksAfterAdd = repo.getTasks();
        assertEquals(1, tasksAfterAdd.size());

        repo.init(true);
        ArrayList<Task> tasksAfterOverwrite = repo.getTasks();
        assertEquals(0, tasksAfterOverwrite.size());
    }
}
