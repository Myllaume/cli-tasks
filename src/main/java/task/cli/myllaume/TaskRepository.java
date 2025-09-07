package task.cli.myllaume;

import java.util.ArrayList;
import java.io.*;

public class TaskRepository {
    private String filePath;
    private String header = "description,completed";
    private ArrayList<CsvError> errors = new ArrayList<>();

    public TaskRepository(String filePath) {
        this.filePath = filePath;
    }

    public ArrayList<Task> read() {
        ArrayList<Task> tasks = new ArrayList<>();

        errors.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(this.filePath))) {

            int lineNumber = 1;

            String header = reader.readLine();
            if (!header.equals(this.header)) {
                errors.add(new CsvError(1, "Format d'en-tête incorrect."));
                return tasks;
            }

            String line;

            lineNumber++;

            while ((line = reader.readLine()) != null) {
                Task task = TaskRepository.parseCsvLine(line, lineNumber);
                if (task != null) {
                    tasks.add(task);
                } else {
                    errors.add(new CsvError(lineNumber, "Ligne mal formée ou erreur de conversion"));
                }

                lineNumber++;
            }
        } catch (IOException e) {
            return tasks;
        }
        return tasks;
    }

    public ArrayList<CsvError> getErrors() {
        return errors;
    }

    private static Task parseCsvLine(String line, int lineNumber) {
        String[] parts = line.split(",");
        if (parts.length != 2) {
            return null;
        }

        try {
            String description = parts[0];
            boolean completed = Boolean.parseBoolean(parts[1]);
            return new Task(lineNumber, description, completed);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}