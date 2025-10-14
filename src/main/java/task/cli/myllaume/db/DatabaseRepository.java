package task.cli.myllaume.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import task.cli.myllaume.utils.Validators;

public abstract class DatabaseRepository {
  protected final String url;

  public DatabaseRepository(String dbPath) {
    Validators.throwNullOrEmptyString(dbPath, "Database path cannot be null or empty");

    String normalizedPath = dbPath.trim();
    if (!normalizedPath.endsWith(System.getProperty("file.separator"))) {
      normalizedPath += System.getProperty("file.separator");
    }

    this.url = "jdbc:sqlite:" + normalizedPath + "tasks.db?foreign_keys=on";
  }

  protected Connection getConnection() throws SQLException {
    return DriverManager.getConnection(url);
  }

  public void initTables() throws SQLException {
    try (Connection conn = getConnection()) {
      try (Statement stmt = conn.createStatement()) {
        createTasksTable(stmt);
        createProjectsTable(stmt);

        stmt.execute("ANALYZE");
      }
    }
  }

  private void createTasksTable(Statement stmt) throws SQLException {
    stmt.execute(
        """
        CREATE TABLE IF NOT EXISTS tasks (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            completed INTEGER NOT NULL,
            fulltext TEXT NOT NULL,
            priority INTEGER NOT NULL,
            created_at INTEGER NOT NULL,
            due_at INTEGER NULL,
            done_at INTEGER NULL,
            parent_id INTEGER NULL,
            project_id INTEGER NOT NULL,
            CONSTRAINT fk_parent FOREIGN KEY (parent_id) REFERENCES tasks(id) ON DELETE CASCADE,
            CONSTRAINT fk_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
        )
        """);
  }

  private void createProjectsTable(Statement stmt) throws SQLException {
    stmt.execute(
        """
        CREATE TABLE IF NOT EXISTS projects (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL UNIQUE,
            fulltext TEXT NOT NULL,
            created_at INTEGER NOT NULL,
            is_current INTEGER NOT NULL DEFAULT 0
        )
        """);
  }

  public String getUrl() {
    return this.url;
  }
}
