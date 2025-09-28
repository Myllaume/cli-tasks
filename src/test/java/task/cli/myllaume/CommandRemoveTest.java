package task.cli.myllaume;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class CommandRemoveTest {

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

        repo.createTask("One", false, TaskPriority.LOW);
        Task task2 = repo.createTask("Two", true, TaskPriority.LOW);

        try {
            System.setErr(new PrintStream(err));
            System.setOut(new PrintStream(out));

            CommandRemove cmd = new CommandRemove(repo);
            cmd.id = String.valueOf(task2.getId());
            cmd.run();
        } finally {
            System.setErr(oldErr);
            System.setOut(oldOut);
        }

        assertEquals("", err.toString());
        assertEquals("La tâche " + task2.getId() + " a été supprimée.\n", out.toString());

        ArrayList<Task> tasks = repo.getTasks(10);
        assertEquals(1, tasks.size());
        assertEquals("One", tasks.get(0).getDescription());
    }

    @Test
    public void testRunFail() throws Exception {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;

        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();

        repo.createTask("One", false, TaskPriority.LOW);
        repo.createTask("Two", true, TaskPriority.LOW);

        try {
            System.setErr(new PrintStream(err));
            System.setOut(new PrintStream(out));

            CommandRemove cmd = new CommandRemove(repo);
            cmd.id = "100"; // does not exist
            cmd.run();
        } finally {
            System.setErr(oldErr);
            System.setOut(oldOut);
        }

        assertEquals("", err.toString());
        assertEquals("Erreur lors de la suppression de la tâche 100.\n", out.toString());

        ArrayList<Task> tasks = repo.getTasks(10);
        assertEquals(2, tasks.size());
    }

}