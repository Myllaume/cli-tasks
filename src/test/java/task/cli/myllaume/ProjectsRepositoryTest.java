package task.cli.myllaume;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;

import task.cli.myllaume.db.ProjectsRepository;

public class ProjectsRepositoryTest {

    /**
     * 
     * @Test
     *       public void testInitCreatesDatabaseFile() throws Exception {
     *       Path tempDir = Files.createTempDirectory("tests");
     *       tempDir.toFile().deleteOnExit();
     * 
     *       String dbPath = tempDir.toString();
     *       ProjectsRepository repo = new ProjectsRepository(dbPath);
     * 
     *       repo.initTables();
     * 
     *       File dbFile = new File(dbPath + System.getProperty("file.separator") +
     *       "tasks.db");
     *       assertTrue(dbFile.exists());
     *       }
     * 
     * @Test
     *       public void testInitCreatesDatabaseTable() throws Exception {
     *       Path tempDir = Files.createTempDirectory("tests");
     *       tempDir.toFile().deleteOnExit();
     * 
     *       String dbPath = tempDir.toString();
     *       ProjectsRepository repo = new ProjectsRepository(dbPath);
     * 
     *       repo.initTables();
     *       String url = repo.getUrl();
     * 
     *       try (Connection conn = DriverManager.getConnection(url)) {
     *       Statement stmt = conn.createStatement();
     *       ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE
     *       type='table' AND name='projects'");
     *       assertTrue(rs.next());
     *       assertEquals("projects", rs.getString("name"));
     *       }
     *       }
     * 
     * @Test
     *       public void testInitWithNonExistentDirectory() throws Exception {
     *       String dbPath = "/tmp/nonexistent/path/";
     *       ProjectsRepository repo = new ProjectsRepository(dbPath);
     * 
     *       try {
     *       repo.initTables();
     *       fail("Should have thrown an exception for non-existent directory");
     *       } catch (Exception e) {
     *       assertNotNull(e);
     *       }
     *       }
     * 
     * @Test
     *       public void testConstructorWithNullPath() {
     *       try {
     *       new ProjectsRepository(null);
     *       fail("Should have thrown NullPointerException for null path");
     *       } catch (NullPointerException e) {
     *       assertEquals("Database path cannot be null or empty", e.getMessage());
     *       }
     *       }
     * 
     * @Test
     *       public void testConstructorWithEmptyPath() {
     *       try {
     *       new ProjectsRepository(" ");
     *       fail("Should have thrown IllegalArgumentException for empty path");
     *       } catch (IllegalArgumentException e) {
     *       assertEquals("Database path cannot be null or empty", e.getMessage());
     *       }
     *       }
     * 
     * @Test
     *       public void testConstructorNormalizesPath() throws Exception {
     *       Path tempDir = Files.createTempDirectory("tests");
     *       tempDir.toFile().deleteOnExit();
     * 
     *       String dbPathWithoutSlash = tempDir.toString();
     *       String dbPathWithSlash = tempDir.toString() + "/";
     * 
     *       ProjectsRepository repo1 = new ProjectsRepository(dbPathWithoutSlash);
     *       ProjectsRepository repo2 = new ProjectsRepository(dbPathWithSlash);
     * 
     *       assertEquals(repo1.getUrl(), repo2.getUrl());
     *       assertTrue(repo1.getUrl().endsWith("tasks.db"));
     *       }
     * 
     */

    @Test
    public void testCreateProjectSuccess() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        ProjectsRepository repo = new ProjectsRepository(dbPath);
        repo.initTables();

        Instant now = Instant.now();
        ProjectDb project = repo.createProject(ProjectData.of("Test Project", now));

        assertEquals("Test Project", project.getName());
        assertTrue(project.getId() > 0);
        assertEquals("testproject", project.getFulltext());
        assertEquals(now.getEpochSecond(), project.getCreatedAt().getEpochSecond());
    }

    @Test
    public void testRemoveProjectSuccess() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        ProjectsRepository repo = new ProjectsRepository(dbPath);
        repo.initTables();

        ProjectDb addedProject = repo.createProject(ProjectData.of("Project to Remove", Instant.now()));
        assertNotNull(addedProject);
        int projectId = addedProject.getId();

        ProjectDb removedProject = repo.removeProject(projectId);

        assertEquals(addedProject.getId(), removedProject.getId());
        assertEquals(addedProject.getName(), removedProject.getName());
        assertEquals(addedProject.getFulltext(), removedProject.getFulltext());
        assertEquals(addedProject.getCreatedAt(), removedProject.getCreatedAt());
        assertNull(repo.getProject(projectId));
    }

    @Test
    public void testRemoveNonExistentProject() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        ProjectsRepository repo = new ProjectsRepository(dbPath);
        repo.initTables();

        try {
            repo.removeProject(999); // ID qui n'existe pas
            fail("Should have thrown UnknownProjectException for non-existent project");
        } catch (UnknownProjectException e) {
            assertEquals("Aucun projet trouvé avec l'ID: 999", e.getMessage());
            assertEquals(999, e.getProjectId());
        }
    }

    @Test
    public void testGetProjectsSuccess() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        ProjectsRepository repo = new ProjectsRepository(dbPath);
        repo.initTables();

        repo.createProject(ProjectData.of("Project D", Instant.now()));
        repo.createProject(ProjectData.of("Project B", Instant.now()));
        repo.createProject(ProjectData.of("Project A", Instant.now()));
        repo.createProject(ProjectData.of("Project C", Instant.now()));

        ArrayList<ProjectDb> projects = repo.getProjects(3);

        assertEquals(3, projects.size());
        assertEquals("Project A", projects.get(0).getName());
        assertEquals("Project B", projects.get(1).getName());
        assertEquals("Project C", projects.get(2).getName());
    }

    @Test
    public void testGetProjectSuccess() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        ProjectsRepository repo = new ProjectsRepository(dbPath);
        repo.initTables();

        Instant now = Instant.now();
        ProjectDb addedProject = repo.createProject(ProjectData.of("Test Project", now));
        assertNotNull(addedProject);
        int projectId = addedProject.getId();

        ProjectDb project = repo.getProject(projectId);

        assertEquals(projectId, project.getId());
        assertEquals("Test Project", project.getName());
        assertEquals("testproject", project.getFulltext());
        assertEquals(now.getEpochSecond(), project.getCreatedAt().getEpochSecond());
    }

    @Test
    public void testGetNonExistentProject() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        ProjectsRepository repo = new ProjectsRepository(dbPath);
        repo.initTables();

        assertNull(repo.getProject(999));
    }

    @Test
    public void testSearchProjects() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        ProjectsRepository repo = new ProjectsRepository(dbPath);
        repo.initTables();

        repo.createProject(ProjectData.of("Web Development", Instant.now()));
        repo.createProject(ProjectData.of("Mobile webapp", Instant.now()));
        repo.createProject(ProjectData.of("Web Design", Instant.now()));
        repo.createProject(ProjectData.of("Desktop Application", Instant.now()));

        assertEquals(3, repo.searchProjects("web", 10).size());
        assertEquals(2, repo.searchProjects("web", 2).size());

    }

    @Test
    public void testSearchProjectsNoResults() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        ProjectsRepository repo = new ProjectsRepository(dbPath);
        repo.initTables();

        repo.createProject(ProjectData.of("Web Development", Instant.now()));
        repo.createProject(ProjectData.of("Mobile App", Instant.now()));

        ArrayList<ProjectDb> projects = repo.searchProjects("nonexistent", 10);

        assertEquals(0, projects.size());
    }

    @Test
    public void testUpdateProjectName() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        ProjectsRepository repo = new ProjectsRepository(tempDir.toString());
        repo.initTables();

        ProjectDb originalProject = repo.createProject(ProjectData.of("Original Project", Instant.now()));

        ProjectDb updatedProject = repo.updateProjectName(originalProject.getId(), "Updated Project");

        assertEquals("Updated Project", updatedProject.getName());
        assertEquals("updatedproject", updatedProject.getFulltext());
        assertEquals(originalProject.getId(), updatedProject.getId());
        assertEquals(originalProject.getCreatedAt(), updatedProject.getCreatedAt());
    }

    @Test
    public void testUpdateProjectNameNonExistent() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        ProjectsRepository repo = new ProjectsRepository(tempDir.toString());
        repo.initTables();

        try {
            repo.updateProjectName(999, "Updated Project");
            fail("Should have thrown UnknownProjectException for non-existent project");
        } catch (UnknownProjectException e) {
            assertEquals("Aucun projet trouvé avec l'ID: 999", e.getMessage());
            assertEquals(999, e.getProjectId());
        }
    }

    @Test
    public void testCountProjects() throws Exception {
        Path tempDir = Files.createTempDirectory("tests");
        tempDir.toFile().deleteOnExit();

        String dbPath = tempDir.toString();
        ProjectsRepository repo = new ProjectsRepository(dbPath);
        repo.initTables();

        assertEquals(0, repo.countProjects());

        repo.createProject(ProjectData.of("Project 1", Instant.now()));
        assertEquals(1, repo.countProjects());

        repo.createProject(ProjectData.of("Project 2", Instant.now()));
        assertEquals(2, repo.countProjects());
    }

}