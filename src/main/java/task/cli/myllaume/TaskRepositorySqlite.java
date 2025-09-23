package task.cli.myllaume;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class TaskRepositorySqlite {
    private final String url;

    public TaskRepositorySqlite(String dbPath) {
        this.url = "jdbc:sqlite:" + dbPath + "tasks.db";
    }

    public void init() throws Exception {
        try (Connection conn = DriverManager.getConnection(url)) {
            Statement stmt = conn.createStatement();
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS tasks (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL,
                            completed BOOLEAN NOT NULL DEFAULT 0,
                            fulltext TEXT NOT NULL
                        )
                    """);
        }
    }

    public Task createTask(String name, boolean completed) throws Exception {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la tâche ne peut pas être vide.");
        }

        String sql = "INSERT INTO tasks (name, completed, fulltext) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setBoolean(2, completed);
            pstmt.setString(3, StringUtils.normalizeString(name));
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    String fulltext = StringUtils.normalizeString(name);
                    return new Task(id, name, completed, fulltext);
                } else {
                    throw new SQLException("Impossible de récupérer l'ID généré.");
                }
            }
        }
    }

    public Task removeTask(int id) throws Exception {
        String selectSql = "SELECT * FROM tasks WHERE id = ?";
        String deleteSql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement selectPstmt = conn.prepareStatement(selectSql);
                PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {

            selectPstmt.setInt(1, id);
            try (ResultSet rs = selectPstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    boolean completed = rs.getBoolean("completed");
                    String fulltext = rs.getString("fulltext");

                    deletePstmt.setInt(1, id);
                    deletePstmt.executeUpdate();

                    return new Task(id, name, completed, fulltext);
                } else {
                    throw new IllegalArgumentException("Aucune tâche trouvée avec l'ID: " + id);
                }
            }
        }
    }

    public Task getTask(int id) throws Exception {
        String sql = "SELECT * FROM tasks WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    boolean completed = rs.getBoolean("completed");
                    String fulltext = rs.getString("fulltext");
                    return new Task(id, name, completed, fulltext);
                } else {
                    throw new IllegalArgumentException("Aucune tâche trouvée avec l'ID: " + id);
                }
            }
        }
    }

    public ArrayList<Task> getTasks(int limit) throws Exception {
        String sql = "SELECT * FROM tasks ORDER BY name ASC LIMIT ?";

        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            ArrayList<Task> tasks = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                boolean completed = rs.getBoolean("completed");
                String fulltext = rs.getString("fulltext");
                tasks.add(new Task(id, name, completed, fulltext));
            }
            return tasks;
        }
    }

    public ArrayList<Task> searchTasks(String keyword, int limit) throws Exception {
        String sql = "SELECT * FROM tasks WHERE name LIKE ? ORDER BY name ASC LIMIT ?";
        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            keyword = StringUtils.normalizeString(keyword);

            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();

            ArrayList<Task> tasks = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                boolean completed = rs.getBoolean("completed");
                String fulltext = rs.getString("fulltext");
                tasks.add(new Task(id, name, completed, fulltext));
            }
            return tasks;
        }
    }

    public Task updateTask(int id, String name, Boolean completed) throws Exception {
        if (name != null && name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la tâche ne peut pas être vide.");
        }

        Task existingTask = getTask(id);
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

        String sql = "UPDATE tasks SET name = ?, completed = ?, fulltext = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setBoolean(2, completed);
            pstmt.setString(3, fulltext);
            pstmt.setInt(4, id);
            pstmt.executeUpdate();

            return new Task(id, name, completed, fulltext);
        }
    }

    public String getUrl() {
        return this.url;
    }

    public int countTasks() throws Exception {
        String sql = "SELECT COUNT(*) AS total FROM tasks";

        try (Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int total = rs.getInt("total");
                System.out.println("Total tasks: " + total);
                return total;
            } else {
                return 0;
            }
        }
    }

    public void importFromCsv(String csvPath) throws Exception {
        TaskRepository repo = new TaskRepository(csvPath);
        ArrayList<TaskCsv> tasks = repo.getTasks();

        if (repo.getErrors().size() > 0) {
            throw new Exception("Le fichier CSV contient des erreurs, l'import est annulé.");
        }

        for (TaskCsv task : tasks) {
            this.createTask(task.getDescription(), task.getCompleted());
        }
    }
}