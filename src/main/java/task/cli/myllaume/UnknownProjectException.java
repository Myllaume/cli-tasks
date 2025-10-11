package task.cli.myllaume;

public class UnknownProjectException extends IllegalArgumentException {
  private final int projectId;

  public UnknownProjectException(int projectId) {
    super("Aucun projet trouvé avec l'ID: " + projectId);
    this.projectId = projectId;
  }

  public int getProjectId() {
    return projectId;
  }
}
