package task.cli.myllaume;

import java.time.Instant;

import task.cli.myllaume.utils.Validators;

public class ProjectDb extends ProjectData {
    private final int id;
    private final String fulltext;

    private ProjectDb(int id, String name, Instant createdAt, String fulltext) {
        super(name, createdAt);
        this.id = id;
        this.fulltext = fulltext;
    }

    public int getId() {
        return id;
    }

    public String getFulltext() {
        return fulltext;
    }

    static public ProjectDb of(int id, String name, Instant createdAt, String fulltext) {
        Validators.throwNullOrNegativeNumber(id, "ID must be a positive integer");
        Validators.throwNullOrEmptyString(name, "Project name cannot be null or empty");
        Validators.throwNullOrEmptyString(fulltext, "Project fulltext cannot be null or empty");

        return new ProjectDb(id, name, createdAt, fulltext);
    }

    static public ProjectDb fromSqlResult(java.sql.ResultSet sqlResult) throws java.sql.SQLException {
        int id = sqlResult.getInt("id");
        String name = sqlResult.getString("name");
        String fulltext = sqlResult.getString("fulltext");
        Instant createdAt = Instant.ofEpochSecond(sqlResult.getLong("created_at"));

        return ProjectDb.of(id, name, createdAt, fulltext);
    }

}
