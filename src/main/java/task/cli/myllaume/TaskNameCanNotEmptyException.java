package task.cli.myllaume;

public class TaskNameCanNotEmptyException extends IllegalArgumentException {
  public TaskNameCanNotEmptyException() {
    super("Le nom de la tâche ne peut pas être vide.");
  }
}
