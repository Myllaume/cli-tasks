package task.cli.myllaume;

import java.util.ArrayList;
import java.io.*;

public class TaskRepository {
    private final String filePath;
    private final String header = "description,completed";
    private ArrayList<CsvError> errors = new ArrayList<>();

    public TaskRepository(String filePath) {
        this.filePath = filePath;
    }

    public void init(boolean overwrite) {
        File file = new File(this.filePath);
        if (!file.exists() || overwrite) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(this.header);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
            errors.add(new CsvError(1, "Fichier introuvable."));
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

            boolean found = false;

            while ((currentLine = reader.readLine()) != null) {
                if (currentLineNumber != lineNumber) {
                    writer.write(currentLine);
                    writer.newLine();
                } else {
                    found = true;
                }
                currentLineNumber++;
            }

            if (!found) {
                throw new IOException("Line number " + lineNumber + " does not exist.");
            }
        }

        if (!inputFile.delete()) {
            throw new IOException("Could not delete original file");
        }
        if (!tempFile.renameTo(inputFile)) {
            throw new IOException("Could not rename temp file");
        }
    }

    public void updateLine(int lineNumber, String description, boolean completed) throws IOException {
        File inputFile = new File(this.filePath);
        File tempFile = new File(inputFile.getAbsolutePath() + ".tmp");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String currentLine;
            int currentLineNumber = 0;

            while ((currentLine = reader.readLine()) != null) {
                if (currentLineNumber == lineNumber) {
                    Task newTask = new Task(lineNumber, description, completed);
                    writer.write(newTask.toCsv());
                } else {
                    writer.write(currentLine);
                }
                writer.newLine();
                currentLineNumber++;
            }
        }

        if (!inputFile.delete()) {
            throw new IOException("Could not delete original file");
        }
        if (!tempFile.renameTo(inputFile)) {
            throw new IOException("Could not rename temp file");
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