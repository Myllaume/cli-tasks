package task.cli.myllaume;

import java.time.Instant;
import java.util.Objects;

import task.cli.myllaume.utils.Validators;

public class ProjectData {
    private final String name;
    private final Instant createdAt;

    protected ProjectData(String name, Instant createdAt) {
        this.name = name;
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    static public ProjectData of(String name, Instant createdAt) {
        Validators.throwNullOrEmptyString(name, "Project name cannot be null or empty");
        Objects.requireNonNull(createdAt, "Project createdAt cannot be null");

        return new ProjectData(name, createdAt);
    }
}
