package task.cli.myllaume;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class CommandRemoveTest {

    @Test
    public void testRun() throws IOException {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;

        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String filePath = tempDir.toString() + "/tasks.csv";
        TaskRepository repo = new TaskRepository(filePath);
        repo.init(false);
        repo.addLineAtEnd(new Task(1, "One", false));
        repo.addLineAtEnd(new Task(2, "Two", true));

        try {
            System.setErr(new PrintStream(err));
            System.setOut(new PrintStream(out));

            CommandRemove cmd = new CommandRemove(repo);
            cmd.id = "2";
            cmd.run();
        } finally {
            System.setErr(oldErr);
            System.setOut(oldOut);
        }

        assertEquals("", err.toString());
        assertEquals("La tâche 2 a été supprimée.\n", out.toString());

        ArrayList<Task> tasks = repo.read();
        assertEquals(0, repo.getErrors().size());
        assertEquals(1, tasks.size());
    }

    @Test
    public void testRunFail() throws IOException {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;

        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String filePath = tempDir.toString() + "/tasks.csv";
        TaskRepository repo = new TaskRepository(filePath);
        repo.init(false);
        repo.addLineAtEnd(new Task(1, "One", false));
        repo.addLineAtEnd(new Task(2, "Two", true));

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

        ArrayList<Task> tasks = repo.read();
        assertEquals(0, repo.getErrors().size());
        assertEquals(2, tasks.size());
    }

}