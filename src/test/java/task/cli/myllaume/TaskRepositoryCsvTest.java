package task.cli.myllaume;

import org.junit.Test;

import task.cli.myllaume.csv.CsvError;
import task.cli.myllaume.csv.CsvParsingException;
import task.cli.myllaume.csv.FileNotExistsException;
import task.cli.myllaume.csv.TaskCsv;
import task.cli.myllaume.csv.TaskRepositoryCsv;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.nio.file.Path;

public class TaskRepositoryCsvTest {

    @Test
    public void testOfValidFile() throws Exception {
        TaskRepositoryCsv repo = TaskRepositoryCsv.of("src/test/resources/valid-tasks.csv");
        ArrayList<TaskCsv> tasks = repo.getTasks();

        assertEquals(2, tasks.size());
    }

    @Test
    public void testOfWithNonCsvFileExtension() {
        try {
            TaskRepositoryCsv.of("src/test/resources/valid-tasks.txt");
            fail("Should have thrown IllegalArgumentException for non-csv extension");
        } catch (IllegalArgumentException e) {
            assertEquals("Le fichier doit avoir l'extension .csv", e.getMessage());
        } catch (Exception e) {
            fail("Should throw IllegalArgumentException, not " + e.getClass().getSimpleName());
        }
    }

    @Test
    public void testOfWithNonExistentFile() {

        try {
            TaskRepositoryCsv repo = TaskRepositoryCsv.of("src/test/resources/unknown.csv");
            repo.getTasks();
            fail("Should have thrown FileNotExistsException for non-existent file");
        } catch (FileNotExistsException e) {
            assertTrue("Message should contain the file path",
                    e.getMessage().contains("src/test/resources/unknown.csv"));
            assertTrue("FilePath should contain the path", e.getFilePath().contains("src/test/resources/unknown.csv"));
        } catch (Exception e) {
            fail("Should throw FileNotExistsException, not " + e.getClass().getSimpleName());
        }
    }

    @Test
    public void testReadWithInvalidLines() throws Exception {
        TaskRepositoryCsv repo = TaskRepositoryCsv.of("src/test/resources/invalid-tasks.csv");

        try {
            repo.getTasks();
            fail("Should have thrown CsvParsingException for invalid lines");
        } catch (CsvParsingException e) {
            ArrayList<CsvError> errors = e.getErrors();
            assertEquals(3, errors.size());
            assertEquals(3, errors.get(0).getLineNumber());
            assertEquals(4, errors.get(1).getLineNumber());
            assertEquals(5, errors.get(2).getLineNumber());
            assertEquals("Ligne mal formée ou erreur de conversion.", errors.get(0).getMessage());
            assertEquals("Le format du champ 'description' est incorrect.", errors.get(1).getMessage());
            assertEquals("Le format du champ 'completed' est incorrect.", errors.get(2).getMessage());
        }
    }

    @Test
    public void testReadWithInvalidHeader() throws Exception {
        TaskRepositoryCsv repo = TaskRepositoryCsv.of("src/test/resources/invalid-header-tasks.csv");

        try {
            repo.getTasks();
            fail("Should have thrown CsvParsingException for invalid header");
        } catch (CsvParsingException e) {
            ArrayList<CsvError> errors = e.getErrors();
            assertEquals(1, errors.size());
            assertEquals(1, errors.get(0).getLineNumber());
            assertEquals("Format d'en-tête incorrect.", errors.get(0).getMessage());
        }
    }

    @Test
    public void testAddLineAtEnd() throws Exception {
        File tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit();

        TaskRepositoryCsv repo = TaskRepositoryCsv.of(tempFile.getAbsolutePath());
        repo.init(true);
        TaskCsv task = new TaskCsv(1, "Test tâche", false);

        repo.addLineAtEnd(task);

        TaskRepositoryCsv repoAfter = TaskRepositoryCsv.of(tempFile.getAbsolutePath());
        ArrayList<TaskCsv> tasks = repoAfter.getTasks();
        assertEquals(1, tasks.size());
        assertEquals("Test tâche", tasks.get(0).getDescription());
        assertFalse(tasks.get(0).getCompleted());
    }

    @Test
    public void testInitWithNewFile() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();
        File newFile = new File(tempDir.toString() + "/tasks.csv");
        assertFalse(newFile.exists());

        TaskRepositoryCsv repo = TaskRepositoryCsv.of(newFile.getAbsolutePath());
        repo.init(false);

        assertTrue(newFile.exists());
        ArrayList<TaskCsv> tasks = repo.getTasks();
        assertEquals(0, tasks.size());

        repo.addLineAtEnd(new TaskCsv(1, "Test", false));
        TaskRepositoryCsv repoAfter = TaskRepositoryCsv.of(newFile.getAbsolutePath());
        ArrayList<TaskCsv> tasksAfterAdd = repoAfter.getTasks();
        assertEquals(1, tasksAfterAdd.size());
    }

    @Test
    public void testInitWithOverwrite() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();
        File newFile = new File(tempDir.toString() + "/tasks.csv");

        TaskRepositoryCsv repo = TaskRepositoryCsv.of(newFile.getAbsolutePath());
        repo.init(false);

        ArrayList<TaskCsv> tasks = repo.getTasks();
        assertEquals(0, tasks.size());

        repo.addLineAtEnd(new TaskCsv(1, "Test", false));
        ArrayList<TaskCsv> tasksAfterAdd = repo.getTasks();
        assertEquals(1, tasksAfterAdd.size());

        repo.init(true);
        ArrayList<TaskCsv> tasksAfterOverwrite = repo.getTasks();
        assertEquals(0, tasksAfterOverwrite.size());
    }

    @Test
    public void testInitIOException() throws Exception {

        try {
            TaskRepositoryCsv repo = TaskRepositoryCsv.of("/nonexistent/directory/tasks.csv");
            repo.init(false);
            fail("Should have thrown IOException for invalid path");
        } catch (IOException e) {

            assertNotNull(e.getMessage());
        } catch (Exception e) {
            fail("Should throw IOException, not " + e.getClass().getSimpleName());
        }
    }

    @Test
    public void testParseCsvLineWithDifferentData() throws Exception {
        TaskRepositoryCsv repo = TaskRepositoryCsv.of("src/test/resources/many.csv");
        ArrayList<TaskCsv> tasks = repo.getTasks();

        assertEquals(52, tasks.size());

        assertEquals("Implement user authentication", tasks.get(0).getDescription());
        assertTrue(tasks.get(0).getCompleted());

        assertEquals("Refactor App.java for readability", tasks.get(2).getDescription());
        assertFalse(tasks.get(2).getCompleted());

        assertEquals("Implement error handling", tasks.get(8).getDescription());
        assertFalse(tasks.get(8).getCompleted());

        assertEquals("Configure Maven Surefire plugin", tasks.get(12).getDescription());
        assertTrue(tasks.get(12).getCompleted());
    }

    @Test
    public void testGetTasksOnNonExistentFileThrowsFileNotExistsException() {
        TaskRepositoryCsv repo = TaskRepositoryCsv.of("src/test/resources/nonexistent.csv");

        try {
            repo.getTasks();
            fail("Should have thrown FileNotExistsException");
        } catch (FileNotExistsException e) {
            assertTrue("Message should contain the file path",
                    e.getMessage().contains("src/test/resources/nonexistent.csv"));
            assertTrue("FilePath should contain the path",
                    e.getFilePath().contains("src/test/resources/nonexistent.csv"));
        } catch (Exception e) {
            fail("Should throw FileNotExistsException, not " + e.getClass().getSimpleName());
        }
    }

}
