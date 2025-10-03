package task.cli.myllaume;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Project {
    private final int id;
    private final String name;

    public Project(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Project fromSqlResult(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");

        return new Project(id, name);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Project of(int id, String name) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be positive");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }

        return new Project(id, name);
    }
}
