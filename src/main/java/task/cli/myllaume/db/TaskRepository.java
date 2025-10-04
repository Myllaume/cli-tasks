package task.cli.myllaume.db;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;

import task.cli.myllaume.Task;
import task.cli.myllaume.TaskPriority;
import task.cli.myllaume.csv.TaskCsv;
import task.cli.myllaume.csv.TaskRepositoryCsv;
import task.cli.myllaume.utils.StringUtils;

public class TaskRepository extends Repository {

    public TaskRepository(String dbPath) {
        super(dbPath);
    }

    public Task createTask(String name, boolean completed, TaskPriority priority, Instant dueDate)
            throws Exception {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la tâche ne peut pas être vide.");
        }

        String sql = """
                INSERT INTO tasks (name, completed, fulltext, priority, due_at, done_at, project_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String fulltext = StringUtils.normalizeString(name);

            pstmt.setString(1, name);
            pstmt.setBoolean(2, completed);
            pstmt.setString(3, fulltext);
            pstmt.setInt(4, priority.getLevel());
            if (dueDate == null) {
                pstmt.setNull(5, java.sql.Types.INTEGER);
            } else {
                pstmt.setLong(5, dueDate.getEpochSecond());
            }
            if (completed) {
                pstmt.setLong(6, Instant.now().getEpochSecond());
            } else {
                pstmt.setNull(6, java.sql.Types.INTEGER);
            }
            // pstmt.setInt(7, projectId);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return getTask(id);
                } else {
                    throw new SQLException("Impossible de récupérer l'ID généré.");
                }
            }
        }
    }

    public Task createSubTask(int parentId, String name, boolean completed, TaskPriority priority, Instant dueDate)
            throws Exception {
        return createSubTask(parentId, name, completed, priority, dueDate, null);
    }

    public Task createSubTask(int parentId, String name, boolean completed, TaskPriority priority, Instant dueDate,
            Integer projectId)
            throws Exception {
        if (name == null || name.trim().isEmpty()) {
            throw new TaskNameCanNotEmptyException();
        }

        Task parentTask = getTask(parentId);
        if (parentTask == null) {
            throw new UnknownTaskException(parentId);
        }

        String sql = "INSERT INTO tasks (name, completed, fulltext, priority, due_at, done_at, parent_id, project_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String fulltext = StringUtils.normalizeString(name);

            pstmt.setString(1, name);
            pstmt.setBoolean(2, completed);
            pstmt.setString(3, fulltext);
            pstmt.setInt(4, priority.getLevel());
            if (dueDate == null) {
                pstmt.setNull(5, java.sql.Types.INTEGER);
            } else {
                pstmt.setLong(5, dueDate.getEpochSecond());
            }
            if (completed) {
                pstmt.setLong(6, Instant.now().getEpochSecond());
            } else {
                pstmt.setNull(6, java.sql.Types.INTEGER);
            }
            pstmt.setInt(7, parentTask.getId());
            if (projectId == null) {
                pstmt.setNull(8, java.sql.Types.INTEGER);
            } else {
                pstmt.setInt(8, projectId);
            }
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return getTask(id);
                } else {
                    throw new SQLException("Impossible de récupérer l'ID généré.");
                }
            }
        }
    }

    public Task removeTask(int id) throws Exception {
        String deleteSql = "DELETE FROM tasks WHERE id = ?";

        Task task = getTask(id);
        if (task == null) {
            throw new UnknownTaskException(id);
        }

        try (Connection conn = getConnection();
                PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {

            deletePstmt.setInt(1, id);
            int affected = deletePstmt.executeUpdate();
            if (affected == 0) {
                throw new IllegalArgumentException("Cannot find task was about to delete");
            }
            return task;
        }
    }

    public Task getTask(int id) throws Exception {
        String sql = "SELECT id, name, completed, fulltext, priority, created_at, due_at, done_at FROM tasks WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Task.fromSqlResult(rs);
                } else {
                    return null;
                }
            }
        }
    }

    public ArrayList<Task> getTasks(int limit) throws Exception {
        String sql = "SELECT id, name, completed, fulltext, priority, created_at, due_at, done_at FROM tasks ORDER BY name ASC LIMIT ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                ArrayList<Task> tasks = new ArrayList<>();
                while (rs.next()) {
                    tasks.add(Task.fromSqlResult(rs));
                }
                return tasks;
            }
        }
    }

    public ArrayList<Task> getTasksOrderByPriority(int dueInDays, int limit) throws Exception {
        String sql = String.format("""
                SELECT id, name, completed, fulltext, priority, created_at, due_at, done_at FROM tasks
                WHERE
                    parent_id IS NULL AND
                    completed = 0
                ORDER BY
                    (due_at IS NOT NULL AND due_at <= strftime('%%s','now','+%d days')) DESC,
                    priority DESC,
                    id ASC
                LIMIT ?
                """, dueInDays);

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                ArrayList<Task> tasks = new ArrayList<>();
                while (rs.next()) {
                    tasks.add(Task.fromSqlResult(rs));
                }
                return tasks;
            }
        }
    }

    public Task getTaskWithSubTasks(int id, int limit) throws Exception {
        Task parentTask = getTask(id);
        if (parentTask == null) {
            throw new UnknownTaskException(id);
        }

        String subTaskSql = "SELECT id, name, completed, fulltext, priority, created_at, due_at, done_at FROM tasks WHERE parent_id = ? ORDER BY name ASC LIMIT ?";
        try (Connection conn = getConnection();
                PreparedStatement subPstmt = conn.prepareStatement(subTaskSql)) {

            subPstmt.setInt(1, id);
            subPstmt.setInt(2, limit);
            try (ResultSet rs = subPstmt.executeQuery()) {
                ArrayList<Task> subTasks = new ArrayList<>();
                while (rs.next()) {
                    subTasks.add(Task.fromSqlResult(rs));
                }
                parentTask.setSubTasks(subTasks);
                return parentTask;
            }
        }
    }

    public Task getLastTask() throws Exception {
        String sql = "SELECT id, name, completed, fulltext, priority, created_at, due_at, done_at FROM tasks WHERE parent_id IS NULL ORDER BY id DESC LIMIT 1";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return Task.fromSqlResult(rs);
            } else {
                return null;
            }
        }
    }

    private ArrayList<Task> searchTasksProcess(String keyword, int limit, String sql) throws Exception {
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            keyword = StringUtils.normalizeString(keyword);

            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setInt(2, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                ArrayList<Task> tasks = new ArrayList<>();
                while (rs.next()) {
                    tasks.add(Task.fromSqlResult(rs));
                }
                return tasks;
            }
        }
    }

    public ArrayList<Task> searchTasks(String keyword, int limit) throws Exception {
        String sql = "SELECT id, name, completed, fulltext, priority, created_at, due_at, done_at FROM tasks WHERE fulltext LIKE ? AND parent_id IS NULL ORDER BY name ASC LIMIT ?";
        return searchTasksProcess(keyword, limit, sql);
    }

    public ArrayList<Task> searchTasksTodo(String keyword, int limit) throws Exception {
        String sql = "SELECT id, name, completed, fulltext, priority, created_at, due_at, done_at FROM tasks WHERE fulltext LIKE ? AND completed = 0 AND parent_id IS NULL ORDER BY name ASC LIMIT ?";
        return searchTasksProcess(keyword, limit, sql);
    }

    public ArrayList<Task> searchTasksDone(String keyword, int limit) throws Exception {
        String sql = "SELECT id, name, completed, fulltext, priority, created_at, due_at, done_at FROM tasks WHERE fulltext LIKE ? AND completed = 1 AND parent_id IS NULL ORDER BY name ASC LIMIT ?";
        return searchTasksProcess(keyword, limit, sql);
    }

    public Task updateTaskName(int id, String name) throws Exception {
        Task existingTask = getTask(id);

        if (existingTask == null) {
            throw new UnknownTaskException(id);
        }

        String fulltext = StringUtils.normalizeString(name);

        String sql = "UPDATE tasks SET name = ?, fulltext = ? WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, fulltext);
            pstmt.setInt(3, id);
            pstmt.executeUpdate();

            return getTask(id);
        }
    }

    public Task updateTaskCompleted(int id, boolean completed) throws Exception {
        Task existingTask = getTask(id);

        if (existingTask == null) {
            throw new UnknownTaskException(id);
        }

        String sql = "UPDATE tasks SET completed = ?, done_at = ? WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, completed);
            if (completed) {
                pstmt.setLong(2, Instant.now().getEpochSecond());
            } else {
                pstmt.setNull(2, java.sql.Types.INTEGER);
            }
            pstmt.setInt(3, id);
            pstmt.executeUpdate();

            return getTask(id);
        }
    }
    
    /**
     * Marque une tâche comme terminée
     */
    public Task markTaskAsDone(int id) throws Exception {
        return updateTaskCompleted(id, true);
    }

    public Task updateTaskPriority(int id, TaskPriority priority) throws Exception {
        Task existingTask = getTask(id);

        if (existingTask == null) {
            throw new UnknownTaskException(id);
        }

        String sql = "UPDATE tasks SET priority = ? WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, priority.getLevel());
            pstmt.setInt(2, id);
            pstmt.executeUpdate();

            return getTask(id);
        }
    }

    public Task updateTaskDueDate(int id, Instant dueDate) throws Exception {
        Task existingTask = getTask(id);

        if (existingTask == null) {
            throw new UnknownTaskException(id);
        }

        String sql = "UPDATE tasks SET due_at = ? WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (dueDate == null) {
                pstmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                pstmt.setLong(1, dueDate.getEpochSecond());
            }
            pstmt.setInt(2, id);
            pstmt.executeUpdate();

            return getTask(id);
        }
    }

    private int executeCountQuery(String sql) throws Exception {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            } else {
                return 0;
            }
        }
    }

    public int countTasks() throws Exception {
        return executeCountQuery("SELECT COUNT(*) AS total FROM tasks");
    }

    public int countTasksTodo() throws Exception {
        return executeCountQuery("SELECT COUNT(*) AS total FROM tasks WHERE completed = 0");
    }

    public int countTasksDone() throws Exception {
        return executeCountQuery("SELECT COUNT(*) AS total FROM tasks WHERE completed = 1");
    }

    public void importFromCsv(String csvPath) throws Exception {
        ArrayList<TaskCsv> tasks;

        try {
            TaskRepositoryCsv repo = TaskRepositoryCsv.of(csvPath);
            tasks = repo.getTasks();
        } catch (Exception e) {
            throw new Exception("Le fichier CSV contient des erreurs, l'import est annulé.");
        }

        Instant now = Instant.now();

        String sql = "INSERT INTO tasks (name, completed, fulltext, created_at, due_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (TaskCsv task : tasks) {
                pstmt.setString(1, task.getDescription());
                pstmt.setBoolean(2, task.getCompleted());
                pstmt.setString(3, StringUtils.normalizeString(task.getDescription()));
                pstmt.setLong(4, now.getEpochSecond());
                if (task.getCompleted()) {
                    pstmt.setLong(5, now.getEpochSecond());
                } else {
                    pstmt.setNull(5, java.sql.Types.INTEGER);
                }
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
        }
    }

    public void exportToCsv(String csvPath, int limit, boolean overwrite) throws Exception {
        ArrayList<Task> tasks = getTasks(limit);

        File csvFile = new File(csvPath);

        TaskRepositoryCsv repo = TaskRepositoryCsv.of(csvFile.getAbsolutePath());
        repo.init(overwrite);

        for (Task task : tasks) {
            TaskCsv taskCsv = new TaskCsv(task.getId(), task.getDescription(), task.getCompleted());
            repo.addLineAtEnd(taskCsv);
        }
    }

    /**
     * Récupère toutes les tâches d'un projet donné
     */
    public ArrayList<Task> getTasksByProject(int projectId, int limit) throws Exception {
        String sql = "SELECT id, name, completed, fulltext, priority, created_at, due_at, done_at FROM tasks WHERE project_id = ? ORDER BY name ASC LIMIT ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, projectId);
            pstmt.setInt(2, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                ArrayList<Task> tasks = new ArrayList<>();
                while (rs.next()) {
                    tasks.add(Task.fromSqlResult(rs));
                }
                return tasks;
            }
        }
    }

    /**
     * Assigne une tâche à un projet
     */
    public Task assignTaskToProject(int taskId, int projectId) throws Exception {
        Task task = getTask(taskId);
        if (task == null) {
            throw new UnknownTaskException(taskId);
        }

        String sql = "UPDATE tasks SET project_id = ? WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, projectId);
            pstmt.setInt(2, taskId);

            int affected = pstmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Aucune ligne affectée lors de l'assignation.");
            }

            return getTask(taskId);
        }
    }

    /**
     * Retire une tâche d'un projet
     */
    public Task unassignTaskFromProject(int taskId) throws Exception {
        Task task = getTask(taskId);
        if (task == null) {
            throw new UnknownTaskException(taskId);
        }

        String sql = "UPDATE tasks SET project_id = NULL WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, taskId);

            int affected = pstmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Aucune ligne affectée lors de la désassignation.");
            }

            return getTask(taskId);
        }
    }
}