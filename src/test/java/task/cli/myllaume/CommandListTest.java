package task.cli.myllaume;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

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
        repo.initTables();
        
        Instant now = Instant.now();
        TaskData taskOne = TaskData.of("One", false, TaskPriority.LOW, now, null, null);
        repo.createTask(taskOne);
        TaskData taskTwo = TaskData.of("Two", true, TaskPriority.LOW, now, null, now);
        repo.createTask(taskTwo);

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
        repo.initTables();
        
        Instant now = Instant.now();
        TaskData taskData = TaskData.of("Test", false, TaskPriority.LOW, now, null, null);
        repo.createTask(taskData);

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