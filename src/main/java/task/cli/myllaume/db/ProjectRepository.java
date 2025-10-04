package task.cli.myllaume.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import task.cli.myllaume.Project;

public class ProjectRepository extends Repository {

    public ProjectRepository(String dbPath) {
        super(dbPath);
    }

    public Project createProject(String name) throws Exception {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du projet ne peut pas être vide.");
        }

        String sql = "INSERT INTO projects (name) VALUES (?)";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, name.trim());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return getProjectById(id);
                } else {
                    throw new SQLException("Impossible de récupérer l'ID généré.");
                }
            }
        }
    }

    public Project getProjectById(int id) throws Exception {
        String sql = "SELECT id, name FROM projects WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Project.fromSqlResult(rs);
                } else {
                    return null;
                }
            }
        }
    }

    public Project getProjectByName(String name) throws Exception {
        String sql = "SELECT id, name FROM projects WHERE name = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Project.fromSqlResult(rs);
                } else {
                    return null;
                }
            }
        }
    }

    public ArrayList<Project> getProjects(int limit) throws Exception {
        String sql = "SELECT id, name FROM projects ORDER BY name ASC LIMIT ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                ArrayList<Project> projects = new ArrayList<>();
                while (rs.next()) {
                    projects.add(Project.fromSqlResult(rs));
                }
                return projects;
            }
        }
    }

    public Project updateProjectName(int id, String name) throws Exception {
        Project project = getProjectById(id);
        if (project == null) {
            throw new IllegalArgumentException("Projet avec l'ID " + id + " non trouvé.");
        }

        String sql = "UPDATE projects SET name = ? WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name != null ? name.trim() : project.getName());
            pstmt.setInt(2, id);

            int affected = pstmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Aucune ligne affectée lors de la mise à jour.");
            }

            return getProjectById(id);
        }
    }

    public Project removeProject(int id) throws Exception {
        Project project = getProjectById(id);
        if (project == null) {
            throw new IllegalArgumentException("Projet avec l'ID " + id + " non trouvé.");
        }

        String sql = "DELETE FROM projects WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affected = pstmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Impossible de supprimer le projet.");
            }

            return project;
        }
    }

    public int countProjects() throws Exception {
        String sql = "SELECT COUNT(*) AS total FROM projects";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        }
    }

}
