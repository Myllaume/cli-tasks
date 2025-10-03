package task.cli.myllaume.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Abstraction for SQLite repositories */
public abstract class Repository {
    protected final String url;
    
    public Repository(String dbPath) {
        if (dbPath == null || dbPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Le chemin de la base de données ne peut pas être nul ou vide.");
        }

        String normalizedPath = dbPath.trim();
        if (!normalizedPath.endsWith(System.getProperty("file.separator"))) {
            normalizedPath += System.getProperty("file.separator");
        }

        this.url = "jdbc:sqlite:" + normalizedPath + "tasks.db";
    }
    
    /**
     * Obtient une connexion à la base de données
     */
    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }
    
    /**
     * Initialise la base de données et toutes les tables
     */
    public void initTables() throws Exception {
        try (Connection conn = getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                // Initialiser toutes les tables
                createTasksTable(stmt);
                createProjectsTable(stmt);
                
                // Optimisations SQLite
                stmt.execute("ANALYZE");
            }
        }
    }
    
    /**
     * Initialise la table des tâches
     */
    private void createTasksTable(Statement stmt) throws SQLException {
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    completed BOOLEAN NOT NULL DEFAULT 0,
                    fulltext TEXT NOT NULL,
                    priority INTEGER NOT NULL DEFAULT 1,
                    created_at INTEGER NOT NULL DEFAULT (strftime('%s','now')),
                    due_at INTEGER NULL,
                    done_at INTEGER NULL,
                    parent_id INTEGER NULL,
                    project_id INTEGER NULL,
                    CONSTRAINT fk_parent FOREIGN KEY (parent_id) REFERENCES tasks(id) ON DELETE CASCADE,
                    CONSTRAINT fk_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL
                )
            """);
    }
    
    /**
     * Initialise la table des projets
     */
    private void createProjectsTable(Statement stmt) throws SQLException {
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS projects (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE
                )
            """);
    }

    public String getUrl() {
        return this.url;
    }
}
