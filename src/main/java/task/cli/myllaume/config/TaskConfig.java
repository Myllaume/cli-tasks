package task.cli.myllaume.config;

public class TaskConfig {
    private final String filePath;
    private final int index;

    public TaskConfig(String filePath, int index) {
        this.filePath = filePath;
        this.index = index;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getIndex() {
        return index;
    }
}
