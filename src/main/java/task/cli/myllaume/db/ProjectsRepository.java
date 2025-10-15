package task.cli.myllaume.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Objects;
import task.cli.myllaume.ProjectData;
import task.cli.myllaume.ProjectDb;
import task.cli.myllaume.UnknownProjectException;
import task.cli.myllaume.utils.StringUtils;

public class ProjectsRepository extends DatabaseRepository {
  public ProjectsRepository(String dbPath) {
    super(dbPath);
  }

  public ProjectDb createProject(ProjectData data) throws Exception {
    String sql =
        """
        INSERT INTO projects (name, fulltext, created_at)
        VALUES (?, ?, ?)
        RETURNING id, name, fulltext, created_at
        """;
    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      String fulltext = StringUtils.normalizeString(data.getName());

      pstmt.setString(1, data.getName());
      pstmt.setString(2, fulltext);
      pstmt.setLong(3, data.getCreatedAt().getEpochSecond());

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return ProjectDb.fromSqlResult(rs);
        } else {
          throw new SQLException("Impossible de récupérer le projet créé.");
        }
      }
    }
  }

  public ProjectDb insertDefaultProjectIfNoneExists(ProjectData data) throws Exception {
    String sql =
        """
        INSERT INTO projects (name, fulltext, created_at, is_current)
        SELECT ?, ?, ?, ?
        WHERE NOT EXISTS (SELECT 1 FROM projects)
        RETURNING id, name, fulltext, created_at
        """;
    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      String fulltext = StringUtils.normalizeString(data.getName());

      pstmt.setString(1, data.getName());
      pstmt.setString(2, fulltext);
      pstmt.setLong(3, data.getCreatedAt().getEpochSecond());
      pstmt.setInt(4, 1);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return ProjectDb.fromSqlResult(rs);
        } else {
          throw new IllegalStateException(
              "Impossible de créer le projet par défaut : un projet existe déjà.");
        }
      }
    }
  }

  public ProjectDb getCurrentProject() throws Exception {
    String sql =
        """
        SELECT id, name, fulltext, created_at,
          (SELECT COUNT(*) FROM projects WHERE is_current = 1) as current_count
        FROM projects
        WHERE is_current = 1
        """;

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {

      if (rs.next()) {
        int currentCount = rs.getInt("current_count");
        if (currentCount > 1) {
          throw new IllegalStateException(
              "Database integrity error: multiple projects marked as current ("
                  + currentCount
                  + ")");
        }
        return ProjectDb.fromSqlResult(rs);
      } else {
        return null;
      }
    }
  }

  public boolean hasCurrentProject() throws Exception {
    String sql = "SELECT EXISTS(SELECT 1 FROM projects WHERE is_current = 1) AS has_current";

    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

      if (rs.next()) {
        return rs.getBoolean("has_current");
      } else {
        return false;
      }
    }
  }

  public ProjectDb updateCurrentProject(int id) throws Exception {
    String clearSql = "UPDATE projects SET is_current = 0 WHERE is_current = 1";
    String setSql =
        "UPDATE projects SET is_current = 1 WHERE id = ? RETURNING id, name, fulltext, created_at";

    try (Connection conn = getConnection()) {
      conn.setAutoCommit(false);

      try (PreparedStatement clearPstmt = conn.prepareStatement(clearSql);
          PreparedStatement setPstmt = conn.prepareStatement(setSql)) {

        clearPstmt.executeUpdate();

        setPstmt.setInt(1, id);
        ProjectDb result;
        try (ResultSet rs = setPstmt.executeQuery()) {
          if (rs.next()) {
            result = ProjectDb.fromSqlResult(rs);
          } else {
            throw new IllegalArgumentException("Cannot find project was about to set as current");
          }
        }

        conn.commit();
        return result;
      } catch (Exception e) {
        conn.rollback();
        throw e;
      } finally {
        conn.setAutoCommit(true);
      }
    }
  }

  public ProjectDb getProject(int id) throws Exception {
    String sql = "SELECT id, name, fulltext, created_at FROM projects WHERE id = ?";

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, id);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return ProjectDb.fromSqlResult(rs);
        } else {
          return null;
        }
      }
    }
  }

  public ProjectDb removeProject(int id) throws Exception {
    ProjectDb currentProject = getCurrentProject();
    Objects.requireNonNull(currentProject, "Current project cannot be null to delete a project");

    if (id == currentProject.getId()) {
      throw new IllegalStateException("Cannot delete the current project");
    }

    String deleteSql = "DELETE FROM projects WHERE id = ? RETURNING id, name, fulltext, created_at";

    try (Connection conn = getConnection();
        PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {

      deletePstmt.setInt(1, id);
      try (ResultSet rs = deletePstmt.executeQuery()) {
        if (rs.next()) {
          return ProjectDb.fromSqlResult(rs);
        } else {
          throw new UnknownProjectException(id);
        }
      }
    }
  }

  public ArrayList<ProjectDb> getProjects(int limit) throws Exception {
    String sql = "SELECT id, name, fulltext, created_at FROM projects ORDER BY name ASC LIMIT ?";

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, limit);
      try (ResultSet rs = pstmt.executeQuery()) {
        ArrayList<ProjectDb> projects = new ArrayList<>();
        while (rs.next()) {
          projects.add(ProjectDb.fromSqlResult(rs));
        }
        return projects;
      }
    }
  }

  public ArrayList<ProjectDb> searchProjects(String keyword, int limit) throws Exception {
    String sql =
        """
        SELECT id, name, fulltext, created_at
        FROM projects
        WHERE fulltext LIKE ?
        ORDER BY name ASC LIMIT ?
        """;

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      keyword = StringUtils.normalizeString(keyword);

      pstmt.setString(1, "%" + keyword + "%");
      pstmt.setInt(2, limit);
      try (ResultSet rs = pstmt.executeQuery()) {
        ArrayList<ProjectDb> projects = new ArrayList<>();
        while (rs.next()) {
          projects.add(ProjectDb.fromSqlResult(rs));
        }
        return projects;
      }
    }
  }

  public ProjectDb updateProjectName(int id, String name) throws Exception {
    String fulltext = StringUtils.normalizeString(name);

    String sql =
        "UPDATE projects SET name = ?, fulltext = ? WHERE id = ? RETURNING id, name, fulltext, created_at";
    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, name);
      pstmt.setString(2, fulltext);
      pstmt.setInt(3, id);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return ProjectDb.fromSqlResult(rs);
        } else {
          throw new UnknownProjectException(id);
        }
      }
    }
  }

  public int countProjects() throws Exception {
    String sql = "SELECT COUNT(*) AS total FROM projects";

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

  public boolean isEmpty() throws Exception {
    return countProjects() == 0;
  }
}
