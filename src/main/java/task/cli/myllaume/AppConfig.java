package task.cli.myllaume;

import java.util.List;

public class AppConfig {
    private final String version;
    private final List<TaskConfig> tasksFiles;

    public AppConfig(String version, List<TaskConfig> tasksFiles) {
        this.version = version;
        this.tasksFiles = tasksFiles;
    }

    public String getVersion() {
        return version;
    }

    public List<TaskConfig> getTasksFiles() {
        return tasksFiles;
    }
}
