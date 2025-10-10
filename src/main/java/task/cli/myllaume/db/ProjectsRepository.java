package task.cli.myllaume.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import task.cli.myllaume.ProjectData;
import task.cli.myllaume.ProjectDb;
import task.cli.myllaume.UnknownProjectException;
import task.cli.myllaume.utils.StringUtils;

public class ProjectsRepository extends DatabaseRepository {
    public ProjectsRepository(String dbPath) {
        super(dbPath);
    }

    public ProjectDb createProject(ProjectData data) throws Exception {
        String sql = """
                INSERT INTO projects (name, fulltext, created_at)
                VALUES (?, ?, ?)
                """;
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String fulltext = StringUtils.normalizeString(data.getName());

            pstmt.setString(1, data.getName());
            pstmt.setString(2, fulltext);
            pstmt.setLong(3, data.getCreatedAt().getEpochSecond());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return getProject(id);
                } else {
                    throw new SQLException("Impossible de récupérer l'ID généré.");
                }
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
        String deleteSql = "DELETE FROM projects WHERE id = ?";

        ProjectDb project = getProject(id);
        if (project == null) {
            throw new UnknownProjectException(id);
        }

        try (Connection conn = getConnection();
                PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {

            deletePstmt.setInt(1, id);
            int affected = deletePstmt.executeUpdate();
            if (affected == 0) {
                throw new IllegalArgumentException("Cannot find project was about to delete");
            }
            return project;
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

    private ArrayList<ProjectDb> searchProjectsProcess(String keyword, int limit, String sql) throws Exception {
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

    public ArrayList<ProjectDb> searchProjects(String keyword, int limit) throws Exception {
        String sql = """
                SELECT id, name, fulltext, created_at
                FROM projects
                WHERE fulltext LIKE ?
                ORDER BY name ASC LIMIT ?
                """;
        return searchProjectsProcess(keyword, limit, sql);
    }

    public ProjectDb updateProjectName(int id, String name) throws Exception {
        ProjectDb existingProject = getProject(id);

        if (existingProject == null) {
            throw new UnknownProjectException(id);
        }

        String fulltext = StringUtils.normalizeString(name);

        String sql = "UPDATE projects SET name = ?, fulltext = ? WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, fulltext);
            pstmt.setInt(3, id);
            pstmt.executeUpdate();

            return getProject(id);
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

    public int countProjects() throws Exception {
        return executeCountQuery("SELECT COUNT(*) AS total FROM projects");
    }
}
