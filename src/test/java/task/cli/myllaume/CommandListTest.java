package task.cli.myllaume;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class CommandListTest {

    @Test
    public void testRunOnelineTrue() throws Exception {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;

        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();
        repo.createTask("One", false, TaskPriority.LOW, null);
        repo.createTask("Two", true, TaskPriority.LOW, null);

        try {
            System.setErr(new PrintStream(err));
            System.setOut(new PrintStream(out));

            CommandList cmd = new CommandList(repo);
            cmd.count = true;
            cmd.run();
        } finally {
            System.setErr(oldErr);
            System.setOut(oldOut);
        }

        assertEquals("", err.toString());
        assertEquals("Done: 1 | To do: 1\n", out.toString());
    }

    @Test
    public void testRunOnelineFalse() throws Exception {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;

        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dbPath);
        repo.init();
        repo.createTask("Test", false, TaskPriority.LOW, null);

        try {
            System.setErr(new PrintStream(err));
            System.setOut(new PrintStream(out));

            CommandList cmd = new CommandList(repo);
            cmd.count = false;
            cmd.run();
        } finally {
            System.setErr(oldErr);
            System.setOut(oldOut);
        }

        assertEquals("", err.toString());
        assertEquals("Liste des tâches\n1. [ ] Test\n", out.toString());
    }

}