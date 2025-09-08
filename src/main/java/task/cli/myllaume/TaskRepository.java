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

    public void addLineAtEnd(Task task) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.filePath, true))) {
            writer.write(task.toCsv());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeLine(int lineNumber) throws IOException {
        File inputFile = new File(this.filePath);
        File tempFile = new File(inputFile.getAbsolutePath() + ".tmp");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String currentLine;
            int currentLineNumber = 1;

            while ((currentLine = reader.readLine()) != null) {
                if (currentLineNumber != lineNumber) {
                    writer.write(currentLine);
                    writer.newLine();
                }
                currentLineNumber++;
            }
        }

        if (!inputFile.delete()) {
            System.out.println("Could not delete original file");
            return;
        }
        if (!tempFile.renameTo(inputFile)) {
            System.out.println("Could not rename temp file");
        }
    }

    public void updateLine(int lineNumber, Task newTask) throws IOException {
        File inputFile = new File(this.filePath);
        File tempFile = new File(inputFile.getAbsolutePath() + ".tmp");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String currentLine;
            int currentLineNumber = 0;

            while ((currentLine = reader.readLine()) != null) {
                if (currentLineNumber == lineNumber) {
                    writer.write(newTask.toCsv());
                } else {
                    writer.write(currentLine);
                }
                writer.newLine();
                currentLineNumber++;
            }
        }

        if (!inputFile.delete()) {
            System.out.println("Could not delete original file");
            return;
        }
        if (!tempFile.renameTo(inputFile)) {
            System.out.println("Could not rename temp file");
        }
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