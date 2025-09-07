package task.cli.myllaume;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;

public class TaskRepositoryTest {
    @Test
    public void testReadValidLines() {
        TaskRepository repo = new TaskRepository("src/test/resources/valid-tasks.csv");
        ArrayList<Task> tasks = repo.read();

        assertEquals(2, tasks.size());
    }

    @Test
    public void testReadWithInvalidLines() {
        TaskRepository repo = new TaskRepository("src/test/resources/invalid-tasks.csv");

        ArrayList<Task> tasks = repo.read();
        assertEquals(1, tasks.size());

        ArrayList<CsvError> errors = repo.getErrors();
        assertEquals(1, errors.size());
        assertEquals(3, errors.get(0).getLineNumber());
        assertEquals("Ligne mal formée ou erreur de conversion", errors.get(0).getMessage());
    }

    @Test
    public void testReadWithInvalidHeader() {
        TaskRepository repo = new TaskRepository("src/test/resources/invalid-header-tasks.csv");

        ArrayList<Task> tasks = repo.read();
        assertEquals(0, tasks.size());

        ArrayList<CsvError> errors = repo.getErrors();
        assertEquals(1, errors.size());
        assertEquals(1, errors.get(0).getLineNumber());
        assertEquals("Format d'en-tête incorrect.", errors.get(0).getMessage());
    }

    @Test()
    public void testReadWithUnknownFile() {
        TaskRepository repo = new TaskRepository("src/test/resources/unknown.csv");

        ArrayList<Task> tasks = repo.read();
        assertEquals(0, tasks.size());
    }
}
