package task.cli.myllaume;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class CommandListTest {

    @Test
    public void testRunOnelineTrue() throws IOException {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;

        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String filePath = tempDir.toString() + "/tasks.csv";
        TaskRepository repo = new TaskRepository(filePath);
        repo.init(false);
        repo.addLineAtEnd(new TaskCsv(1, "One", false));
        repo.addLineAtEnd(new TaskCsv(1, "Two", true));

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
    public void testRunOnelineFalse() throws IOException {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;

        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String filePath = tempDir.toString() + "/tasks.csv";
        TaskRepository repo = new TaskRepository(filePath);
        repo.init(false);
        repo.addLineAtEnd(new TaskCsv(1, "Test", false));

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
        assertEquals("Liste des tâches\n- [✗] Test\n", out.toString());
    }

}