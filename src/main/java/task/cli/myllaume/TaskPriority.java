package task.cli.myllaume;

public enum TaskPriority {
    LOW(1, "faible"),
    MEDIUM(2, "moyenne"),
    HIGH(3, "haute"),
    CRITICAL(4, "critique");

    private final int level;
    private final String label;

    TaskPriority(int level, String label) {
        this.level = level;
        this.label = label;
    }

    public int getLevel() {
        return level;
    }

    public String getLabel() {
        return label;
    }

    public static TaskPriority fromLevel(int level) {
        for (TaskPriority p : values()) {
            if (p.level == level) {
                return p;
            }
        }
        throw new IllegalArgumentException("Niveau inconnu : " + level);
    }
   
}
