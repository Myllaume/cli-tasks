package task.cli.myllaume;

import org.junit.Test;

import picocli.CommandLine;

import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class CommandSearchTest {

    @Test
    public void testRunWithMaxResult() throws IOException {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;

        File tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit();

        Files.copy(
                Paths.get("src/test/resources/many.csv"),
                tempFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        TaskRepository repo = new TaskRepository(tempFile.getAbsolutePath());
        ArrayList<TaskCsv> tasks = repo.getTasks();
        assertEquals(52, tasks.size());

        try {
            System.setErr(new PrintStream(err));
            System.setOut(new PrintStream(out));

            CommandSearch cmd = new CommandSearch(repo);
            new CommandLine(cmd).parseArgs("test");
            cmd.run();
        } finally {
            System.setErr(oldErr);
            System.setOut(oldOut);
        }

        assertEquals("", err.toString());
        assertEquals(" 3. [✓] Write unit tests for TaskCsv class\n" +
                "11. [✓] Write integration tests\n" +
                "23. [✗] Write tests for edge cases\n" +
                "33. [✓] Write tests for invalid CSV headers\n" +
                "41. [✓] Write tests for TaskRepository\n" +
                "Recherche terminée. Affichage de 5 résultats sur 9 trouvés.\n", out.toString());
    }

    @Test
    public void testRunWithAllResults() throws IOException {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;

        File tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit();

        Files.copy(
                Paths.get("src/test/resources/many.csv"),
                tempFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        TaskRepository repo = new TaskRepository(tempFile.getAbsolutePath());
        ArrayList<TaskCsv> tasks = repo.getTasks();
        assertEquals(52, tasks.size());

        try {
            System.setErr(new PrintStream(err));
            System.setOut(new PrintStream(out));

            CommandSearch cmd = new CommandSearch(repo);
            new CommandLine(cmd).parseArgs("cli");
            cmd.run();
        } finally {
            System.setErr(oldErr);
            System.setOut(oldOut);
        }

        assertEquals("", err.toString());
        assertEquals("34. [✓] Improve CLI argument parsing\n" +
                "35. [✓] Add color output to CLI\n" +
                "Recherche terminée. Affichage de 2 résultats sur 2 trouvés.\n", out.toString());
    }

    @Test
    public void testRunMessageForEmptySearch() throws IOException {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;

        File tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit();

        Files.copy(
                Paths.get("src/test/resources/many.csv"),
                tempFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        TaskRepository repo = new TaskRepository(tempFile.getAbsolutePath());
        ArrayList<TaskCsv> tasks = repo.getTasks();
        assertEquals(52, tasks.size());

        try {
            System.setErr(new PrintStream(err));
            System.setOut(new PrintStream(out));

            CommandSearch cmd = new CommandSearch(repo);
            cmd.fulltext = "";
            cmd.maxCount = 5;
            cmd.maxResults = 3;
            cmd.run();
        } finally {
            System.setErr(oldErr);
            System.setOut(oldOut);
        }

        assertEquals("", err.toString());
        assertEquals("Aucune chaîne de recherche fournie.\n", out.toString());
    }

    @Test
    public void testRunWithMaxResultCount() throws IOException {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;

        File tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit();

        Files.copy(
                Paths.get("src/test/resources/many.csv"),
                tempFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        TaskRepository repo = new TaskRepository(tempFile.getAbsolutePath());
        ArrayList<TaskCsv> tasks = repo.getTasks();
        assertEquals(52, tasks.size());

        try {
            System.setErr(new PrintStream(err));
            System.setOut(new PrintStream(out));

            CommandSearch cmd = new CommandSearch(repo);
            cmd.fulltext = "test";
            cmd.maxResults = 3;
            cmd.maxCount = 5;
            cmd.run();
        } finally {
            System.setErr(oldErr);
            System.setOut(oldOut);
        }

        assertEquals("", err.toString());
        assertEquals(" 3. [✓] Write unit tests for TaskCsv class\n" +
                "11. [✓] Write integration tests\n" +
                "23. [✗] Write tests for edge cases\n" +
                "Recherche terminée. Affichage de 3 résultats sur 5 trouvés.\n", out.toString());
    }

    @Test
    public void testRunMessageForMaxCountGreaterThanResults() throws IOException {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;

        File tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit();

        Files.copy(
                Paths.get("src/test/resources/many.csv"),
                tempFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        TaskRepository repo = new TaskRepository(tempFile.getAbsolutePath());
        ArrayList<TaskCsv> tasks = repo.getTasks();
        assertEquals(52, tasks.size());

        try {
            System.setErr(new PrintStream(err));
            System.setOut(new PrintStream(out));

            CommandSearch cmd = new CommandSearch(repo);
            cmd.fulltext = "test";
            cmd.maxResults = 5;
            cmd.maxCount = 3;
            cmd.run();
        } finally {
            System.setErr(oldErr);
            System.setOut(oldOut);
        }

        assertEquals("", err.toString());
        assertEquals(
                "Le nombre maximum de résultats affiché ne peut pas être inférieur au nombre maximum de résultats à analyser.\n",
                out.toString());
    }

}