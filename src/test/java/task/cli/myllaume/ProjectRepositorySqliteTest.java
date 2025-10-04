package task.cli.myllaume;

import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import task.cli.myllaume.db.ProjectRepository;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class ProjectRepositorySqliteTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private ProjectRepository repository;
    private String dbPath;

    @Before
    public void setUp() throws Exception {
        dbPath = tempFolder.getRoot().getAbsolutePath();
        repository = new ProjectRepository(dbPath);
        repository.initTables();
    }

    @Test
    public void testCreateProject() throws Exception {
        String name = "Mon Premier Projet";

        Project project = repository.createProject(name);

        assertEquals(name, project.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateProjectWithNullName() throws Exception {
        String name = null;

        repository.createProject(name);
    }

    @Test
    public void testGetProjectById() throws Exception {

        Project created = repository.createProject("Test Project");
        Project project = repository.getProjectById(created.getId());

        assertEquals(created.getId(), project.getId());
        assertEquals(created.getName(), project.getName());
    }

    @Test
    public void testGetProjectByName() throws Exception {

        Project created = repository.createProject("Test Project");
        Project project = repository.getProjectByName(created.getName());

        assertEquals(created.getId(), project.getId());
        assertEquals(created.getName(), project.getName());
    }

    @Test
    public void testGetProjectByIdNotFound() throws Exception {

        Project project = repository.getProjectById(999);

        assertNull(project);
    }

    @Test
    public void testGetProjectByNameNotFound() throws Exception {

        Project project = repository.getProjectByName("Inexistant Project");

        assertNull(project);
    }

    @Test
    public void testGetProjects() throws Exception {

        repository.createProject("Project A");
        repository.createProject("Project B");
        repository.createProject("Project C");

        ArrayList<Project> projects = repository.getProjects(2);

        assertEquals(2, projects.size());
        assertEquals("Project A", projects.get(0).getName());
        assertEquals("Project B", projects.get(1).getName());
    }

    @Test
    public void testUpdateProject() throws Exception {
        Project project = repository.createProject("Project A");
        String newName = "Nouveau Nom";

        Project updated = repository.updateProjectName(project.getId(), newName);

        assertEquals(project.getId(), updated.getId());
        assertEquals(newName, updated.getName());
    }

    @Test
    public void testRemoveProject() throws Exception {

        Project project = repository.createProject("Projet à Supprimer");
        Project removed = repository.removeProject(project.getId());

        assertEquals(project.getId(), removed.getId());
        assertEquals(project.getName(), removed.getName());

        Project shouldBeNull = repository.getProjectById(project.getId());
        assertNull(shouldBeNull);
    }

    @Test
    public void testCountProjects() throws Exception {

        assertEquals(0, repository.countProjects());

        repository.createProject("Projet 1");
        repository.createProject("Projet 2");

        assertEquals(2, repository.countProjects());
    }

}
