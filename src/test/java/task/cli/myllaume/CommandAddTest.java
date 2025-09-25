package task.cli.myllaume;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class CommandAddTest {

    @Test
    public void testRun() throws Exception {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;

        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        try {
            System.setErr(new PrintStream(err));
            System.setOut(new PrintStream(out));

            CommandAdd cmd = new CommandAdd(repo);
            cmd.description = "Test";
            cmd.completed = false;
            cmd.run();
        } finally {
            System.setErr(oldErr);
            System.setOut(oldOut);
        }

        assertEquals("", err.toString());
        assertEquals("La tâche Test a été ajoutée.\n", out.toString());

        ArrayList<Task> tasks = repo.getTasks(10);
        assertEquals(1, tasks.size());
        Task task = tasks.get(0);
        assertEquals("Test", task.getDescription());
        assertFalse(task.getCompleted());
    }

    @Test
    public void testRunCompleted() throws Exception {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;

        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        try {
            System.setErr(new PrintStream(err));
            System.setOut(new PrintStream(out));

            CommandAdd cmd = new CommandAdd(repo);
            cmd.description = "Test";
            cmd.completed = true;
            cmd.run();
        } finally {
            System.setErr(oldErr);
            System.setOut(oldOut);
        }

        assertEquals("", err.toString());
        assertEquals("La tâche Test a été ajoutée.\n", out.toString());

        ArrayList<Task> tasks = repo.getTasks(10);
        assertEquals(1, tasks.size());
        Task task = tasks.get(0);
        assertEquals("Test", task.getDescription());
        assertTrue(task.getCompleted());
    }

}