package task.cli.myllaume.db;

import task.cli.myllaume.Project;
import task.cli.myllaume.Task;
import task.cli.myllaume.TaskPriority;

/**
 * Factory to init and manage repositories.
 * Inter-requests between repositories.
 */

public class RepositoryManager {
    private final String dbPath;
    private TaskRepository taskRepository;
    private ProjectRepository projectRepository;

    public RepositoryManager(String dbPath) {
        this.dbPath = dbPath;
        taskRepository = new TaskRepository(dbPath);
        projectRepository = new ProjectRepository(dbPath);
    }

    /**
     * Init the database tables
     */
    public void init() throws Exception {
        Repository tempRepo = new Repository(dbPath) {
        };
        tempRepo.initTables();

    }

    public TaskRepository getTaskRepository() {
        return taskRepository;
    }

    public ProjectRepository getProjectRepository() {
        return projectRepository;
    }

    public Task assignTaskToProject(int taskId, int projectId) throws Exception {
        Project project = getProjectRepository().getProjectById(projectId);
        if (project == null) {
            throw new IllegalArgumentException("Projet avec l'ID " + projectId + " non trouvé.");
        }

        return getTaskRepository().assignTaskToProject(taskId, projectId);
    }

    public Task createTaskInProject(String taskName, boolean completed,
            TaskPriority priority, java.time.Instant dueDate,
            int projectId) throws Exception {
        Project project = getProjectRepository().getProjectById(projectId);
        if (project == null) {
            throw new IllegalArgumentException("Projet avec l'ID " + projectId + " non trouvé.");
        }

        return getTaskRepository().createTask(taskName, completed, priority, dueDate);
    }

    public Task createSubTaskInProject(int parentId, String taskName, boolean completed,
            TaskPriority priority, java.time.Instant dueDate,
            int projectId) throws Exception {
        Project project = getProjectRepository().getProjectById(projectId);
        if (project == null) {
            throw new IllegalArgumentException("Projet avec l'ID " + projectId + " non trouvé.");
        }

        return getTaskRepository().createSubTask(parentId, taskName, completed, priority, dueDate, projectId);
    }
}
