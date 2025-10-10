package task.cli.myllaume;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import task.cli.myllaume.db.DatabaseRepository;

public class DatabaseRepositoryTest {

    private static class TestDatabaseRepository extends DatabaseRepository {
        public TestDatabaseRepository(String dbPath) {
            super(dbPath);
        }
    }

    @Test
    public void testInitCreatesDatabaseFile() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TestDatabaseRepository repo = new TestDatabaseRepository(dbPath);

        repo.initTables();

        File dbFile = new File(dbPath + System.getProperty("file.separator") + "tasks.db");
        assertTrue(dbFile.exists());
    }

    @Test
    public void testInitCreatesDatabaseTables() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TestDatabaseRepository repo = new TestDatabaseRepository(dbPath);

        repo.initTables();
        String url = repo.getUrl();

        try (Connection conn = DriverManager.getConnection(url)) {
            Statement stmt = conn.createStatement();

            ResultSet rsTasksTable = stmt
                    .executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='tasks'");
            assertTrue("Table 'tasks' should exist", rsTasksTable.next());
            assertEquals("tasks", rsTasksTable.getString("name"));

            ResultSet rsProjectsTable = stmt
                    .executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='projects'");
            assertTrue("Table 'projects' should exist", rsProjectsTable.next());
            assertEquals("projects", rsProjectsTable.getString("name"));
        }
    }

    @Test
    public void testInitWithNonExistentDirectory() throws Exception {
        String dbPath = "/tmp/nonexistent/path/";
        TestDatabaseRepository repo = new TestDatabaseRepository(dbPath);

        try {
            repo.initTables();
            fail("Should have thrown an exception for non-existent directory");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testConstructorWithNullPath() {
        try {
            new TestDatabaseRepository(null);
            fail("Should have thrown NullPointerException for null path");
        } catch (NullPointerException e) {
            assertEquals("Database path cannot be null or empty", e.getMessage());
        }
    }

    @Test
    public void testConstructorWithEmptyPath() {
        try {
            new TestDatabaseRepository("   ");
            fail("Should have thrown IllegalArgumentException for empty path");
        } catch (IllegalArgumentException e) {
            assertEquals("Database path cannot be null or empty", e.getMessage());
        }
    }

    @Test
    public void testConstructorWithWhitespaceOnlyPath() {
        try {
            new TestDatabaseRepository("   \t\n   ");
            fail("Should have thrown IllegalArgumentException for whitespace-only path");
        } catch (IllegalArgumentException e) {
            assertEquals("Database path cannot be null or empty", e.getMessage());
        }
    }

    @Test
    public void testConstructorNormalizesPath() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPathWithoutSlash = tempDir.toString();
        String dbPathWithSlash = tempDir.toString() + "/";

        TestDatabaseRepository repo1 = new TestDatabaseRepository(dbPathWithoutSlash);
        TestDatabaseRepository repo2 = new TestDatabaseRepository(dbPathWithSlash);

        assertEquals(repo1.getUrl(), repo2.getUrl());
        assertTrue(repo1.getUrl().endsWith("tasks.db"));
    }

    @Test
    public void testGetUrlFormat() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TestDatabaseRepository repo = new TestDatabaseRepository(dbPath);

        String url = repo.getUrl();
        assertTrue("URL should start with jdbc:sqlite:", url.startsWith("jdbc:sqlite:"));
        assertTrue("URL should end with tasks.db", url.endsWith("tasks.db"));
        assertTrue("URL should contain the path", url.contains(dbPath));
    }

    @Test
    public void testInitTablesMultipleTimes() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TestDatabaseRepository repo = new TestDatabaseRepository(dbPath);

        repo.initTables();

        repo.initTables();

        String url = repo.getUrl();
        try (Connection conn = DriverManager.getConnection(url)) {
            Statement stmt = conn.createStatement();
            ResultSet rsTasksTable = stmt
                    .executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='tasks'");
            assertTrue(rsTasksTable.next());
            ResultSet rsProjectsTable = stmt
                    .executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='projects'");
            assertTrue(rsProjectsTable.next());
        }
    }

    @Test
    public void testThrowIfDbDoesNotExistWithUninitializedDb() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TestDatabaseRepository repo = new TestDatabaseRepository(dbPath);

        try {
            repo.throwIfDbDoesNotExist();
            fail("Should have thrown SQLException for uninitialized database");
        } catch (SQLException e) {
            assertEquals("Database is not initialized", e.getMessage());
        }
    }

    @Test
    public void testThrowIfDbDoesNotExistWithInitializedDb() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TestDatabaseRepository repo = new TestDatabaseRepository(dbPath);

        repo.initTables();

        repo.throwIfDbDoesNotExist();
    }

    @Test
    public void testThrowIfDbDoesNotExistWithPartiallyInitializedDb() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TestDatabaseRepository repo = new TestDatabaseRepository(dbPath);

        String url = repo.getUrl();
        try (Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE tasks (id INTEGER PRIMARY KEY)");
        }

        try {
            repo.throwIfDbDoesNotExist();
            fail("Should have thrown SQLException for partially initialized database");
        } catch (SQLException e) {
            assertEquals("Database is not initialized", e.getMessage());
        }
    }

    @Test
    public void testDatabaseContainsOnlyExpectedTables() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        TestDatabaseRepository repo = new TestDatabaseRepository(dbPath);

        repo.initTables();
        String url = repo.getUrl();

        try (Connection conn = DriverManager.getConnection(url)) {
            Statement stmt = conn.createStatement();

            ResultSet rsAllAppTables = stmt.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name");

            assertTrue("Should have 'projects' table", rsAllAppTables.next());
            assertEquals("projects", rsAllAppTables.getString("name"));

            assertTrue("Should have 'tasks' table", rsAllAppTables.next());
            assertEquals("tasks", rsAllAppTables.getString("name"));

            assertFalse("Should not have more than 2 application tables", rsAllAppTables.next());
        }
    }
}