package task.cli.myllaume;

import org.junit.Test;

import picocli.CommandLine;

import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;

public class CommandSearchTest {

    @Test
    public void testRunWithMaxResult() throws Exception {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;

        File tempDir = Files.createTempDirectory("tests").toFile();
        tempDir.deleteOnExit();

        TaskRepositorySqlite repo = new TaskRepositorySqlite(tempDir.getAbsolutePath());
        repo.init();

        repo.importFromCsv("src/test/resources/many.csv");

        ArrayList<Task> tasks = repo.getTasks(100);
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
        String output = out.toString();

        assertTrue("Doit contenir 'Write integration tests'", output.contains("Write integration tests"));
        assertTrue("Doit contenir 'Write tests for edge cases'", output.contains("Write tests for edge cases"));
        assertTrue("Doit contenir 'Write tests for TaskRepository'", output.contains("Write tests for TaskRepository"));
        assertTrue("Doit contenir le message de fin",
                output.contains("Recherche terminée. Affichage de 5 résultats sur"));

        // Vérifier qu'il y a exactement 5 lignes de résultats (plus la ligne de fin)
        String[] lines = output.split("\n");
        assertEquals("Devrait avoir 6 lignes (5 résultats + message final)", 6, lines.length);
    }

    @Test
    public void testRunWithAllResults() throws Exception {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;

        File tempDir = Files.createTempDirectory("tests").toFile();
        tempDir.deleteOnExit();

        TaskRepositorySqlite repo = new TaskRepositorySqlite(tempDir.getAbsolutePath());
        repo.init();

        repo.importFromCsv("src/test/resources/many.csv");

        ArrayList<Task> tasks = repo.getTasks(100);
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
        String output = out.toString();

        assertTrue("Doit contenir 'CLI'", output.contains("CLI"));
        assertTrue("Doit contenir le message de fin",
                output.contains("Recherche terminée. Affichage de 2 résultats sur 2 trouvés."));

        String[] lines = output.split("\n");
        assertEquals("Devrait avoir 3 lignes (2 résultats + message final)", 3, lines.length);
    }

    @Test
    public void testRunMessageForEmptySearch() throws Exception {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;

        File tempDir = Files.createTempDirectory("tests").toFile();
        tempDir.deleteOnExit();

        TaskRepositorySqlite repo = new TaskRepositorySqlite(tempDir.getAbsolutePath());
        repo.init();

        repo.importFromCsv("src/test/resources/many.csv");

        ArrayList<Task> tasks = repo.getTasks(100);
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
    public void testRunWithMaxResultCount() throws Exception {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;

        File tempDir = Files.createTempDirectory("tests").toFile();
        tempDir.deleteOnExit();

        TaskRepositorySqlite repo = new TaskRepositorySqlite(tempDir.getAbsolutePath());
        repo.init();
        repo.importFromCsv("src/test/resources/many.csv");

        ArrayList<Task> tasks = repo.getTasks(100);
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
        String output = out.toString();

        assertTrue("Doit contenir 'test'", output.toLowerCase().contains("test"));
        assertTrue("Doit contenir le message de fin",
                output.contains("Recherche terminée. Affichage de 3 résultats sur"));


        String[] lines = output.split("\n");
        assertEquals("Devrait avoir 4 lignes (3 résultats + message final)", 4, lines.length);
    }

    @Test
    public void testRunMessageForMaxCountGreaterThanResults() throws Exception {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;

        File tempDir = Files.createTempDirectory("tests").toFile();
        tempDir.deleteOnExit();

        TaskRepositorySqlite repo = new TaskRepositorySqlite(tempDir.getAbsolutePath());
        repo.init();
        repo.importFromCsv("src/test/resources/many.csv");

        ArrayList<Task> tasks = repo.getTasks(100);
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