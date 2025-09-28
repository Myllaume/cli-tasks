package task.cli.myllaume;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import task.cli.myllaume.csv.TaskCsv;
import task.cli.myllaume.csv.TaskRepository;
import task.cli.myllaume.utils.StringUtils;

public class TaskRepositorySqlite {
    private final String url;

    public TaskRepositorySqlite(String dbPath) {
        if (dbPath == null || dbPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Le chemin de la base de données ne peut pas être nul ou vide.");
        }

        String normalizedPath = dbPath.trim();
        if (!normalizedPath.endsWith(System.getProperty("file.separator"))) {
            normalizedPath += System.getProperty("file.separator");
        }

        this.url = "jdbc:sqlite:" + normalizedPath + "tasks.db";
    }

    public void init() throws Exception {
        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("""
                            CREATE TABLE IF NOT EXISTS tasks (
                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                name TEXT NOT NULL,
                                completed BOOLEAN NOT NULL DEFAULT 0,
                                fulltext TEXT NOT NULL,
                                priority INTEGER NOT NULL DEFAULT 1
                            )
                        """);
                // Improve concurrency and planner accuracy for SQLite
                // stmt.execute("PRAGMA journal_mode = WAL");
                // stmt.execute("PRAGMA synchronous = NORMAL");
                stmt.execute("ANALYZE");
            }
        }
    }

    public Task createTask(String name, boolean completed, TaskPriority priority) throws Exception {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la tâche ne peut pas être vide.");
        }

        String sql = "INSERT INTO tasks (name, completed, fulltext, priority) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String fulltext = StringUtils.normalizeString(name);

            pstmt.setString(1, name);
            pstmt.setBoolean(2, completed);
            pstmt.setString(3, fulltext);
            pstmt.setInt(4, priority.getLevel());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return new Task(id, name, completed, fulltext, priority);
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
            throw new IllegalArgumentException("Aucune tâche trouvée avec l'ID: " + id);
        }

        try (Connection conn = DriverManager.getConnection(url);
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
        String sql = "SELECT id, name, completed, fulltext, priority FROM tasks WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url);
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
        String sql = "SELECT id, name, completed, fulltext, priority FROM tasks ORDER BY name ASC LIMIT ?";

        try (Connection conn = DriverManager.getConnection(url);
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

    public Task getLastTask() throws Exception {
        String sql = "SELECT id, name, completed, fulltext, priority FROM tasks ORDER BY id DESC LIMIT 1";

        try (Connection conn = DriverManager.getConnection(url);
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
        try (Connection conn = DriverManager.getConnection(url);
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
        String sql = "SELECT id, name, completed, fulltext, priority FROM tasks WHERE fulltext LIKE ? ORDER BY name ASC LIMIT ?";
        return searchTasksProcess(keyword, limit, sql);
    }

    public ArrayList<Task> searchTasksTodo(String keyword, int limit) throws Exception {
        String sql = "SELECT id, name, completed, fulltext, priority FROM tasks WHERE fulltext LIKE ? AND completed = 0 ORDER BY name ASC LIMIT ?";
        return searchTasksProcess(keyword, limit, sql);
    }

    public ArrayList<Task> searchTasksDone(String keyword, int limit) throws Exception {
        String sql = "SELECT id, name, completed, fulltext, priority FROM tasks WHERE fulltext LIKE ? AND completed = 1 ORDER BY name ASC LIMIT ?";
        return searchTasksProcess(keyword, limit, sql);
    }

    public Task updateTask(int id, String name, Boolean completed, TaskPriority priority) throws Exception {
        Task existingTask = getTask(id);

        if (existingTask == null) {
            throw new IllegalArgumentException("Aucune tâche trouvée avec l'ID: " + id);
        }

        if (name != null && name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la tâche ne peut pas être vide.");
        }

        String fulltext;

        if (name == null) {
            name = existingTask.getDescription();
            fulltext = existingTask.getFulltext();
        } else {
            fulltext = StringUtils.normalizeString(name);
        }

        if (completed == null) {
            completed = existingTask.getCompleted();
        }
        if (priority == null) {
            priority = existingTask.getPriority();
        }

        String sql = "UPDATE tasks SET name = ?, completed = ?, fulltext = ?, priority = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setBoolean(2, completed);
            pstmt.setString(3, fulltext);
            pstmt.setInt(4, priority.getLevel());
            pstmt.setInt(5, id);
            pstmt.executeUpdate();

            return new Task(id, name, completed, fulltext, priority);
        }
    }

    public String getUrl() {
        return this.url;
    }

    private int executeCountQuery(String sql) throws Exception {
        try (Connection conn = DriverManager.getConnection(url);
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
        TaskRepository repo = new TaskRepository(csvPath);
        ArrayList<TaskCsv> tasks = repo.getTasks();

        if (repo.getErrors().size() > 0) {
            throw new Exception("Le fichier CSV contient des erreurs, l'import est annulé.");
        }

        // Batch insert inside a single transaction to improve performance
        String sql = "INSERT INTO tasks (name, completed, fulltext) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (TaskCsv task : tasks) {
                pstmt.setString(1, task.getDescription());
                pstmt.setBoolean(2, task.getCompleted());
                pstmt.setString(3, StringUtils.normalizeString(task.getDescription()));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
        }
    }
}