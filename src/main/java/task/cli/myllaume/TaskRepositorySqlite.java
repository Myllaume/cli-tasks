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
                            completed BOOLEAN NOT NULL DEFAULT 0
                        )
                    """);
        }
    }

    public Task createTask(String name, boolean completed) throws Exception {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la tâche ne peut pas être vide.");
        }

        String sql = "INSERT INTO tasks (name, completed) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setBoolean(2, completed);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return new Task(id, name, completed);
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

                    deletePstmt.setInt(1, id);
                    deletePstmt.executeUpdate();

                    return new Task(id, name, completed);
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
                    return new Task(id, name, completed);
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
                tasks.add(new Task(id, name, completed));
            }
            return tasks;
        }
    }

    public Task updateTask(int id, String name, Boolean completed) throws Exception {
        if (name != null && name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la tâche ne peut pas être vide.");
        }

        Task existingTask = getTask(id);

        if (name == null) {
            name = existingTask.getDescription();
        }
        if (completed == null) {
            completed = existingTask.getCompleted();
        }

        String sql = "UPDATE tasks SET name = ?, completed = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setBoolean(2, completed);
            pstmt.setInt(3, id);
            pstmt.executeUpdate();

            return new Task(id, name, completed);
        }
    }

    public String getUrl() {
        return this.url;
    }
}