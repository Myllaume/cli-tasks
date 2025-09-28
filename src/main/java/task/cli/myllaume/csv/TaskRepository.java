package task.cli.myllaume.csv;

import java.util.ArrayList;

import task.cli.myllaume.utils.StringUtils;
import java.io.*;

public class TaskRepository {
    private final String filePath;
    private final String header = "description,completed";
    private ArrayList<CsvError> errors = new ArrayList<>();

    public TaskRepository(String filePath) {
        this.filePath = filePath;
    }

    public void init(boolean overwrite) throws IOException {
        File file = new File(this.filePath);
        if (!file.exists() || overwrite) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(this.header);
                writer.newLine();
            }
        }
    }

    private ArrayList<TaskCsv> read() throws Exception {
        ArrayList<TaskCsv> tasks = new ArrayList<>();

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
                TaskCsv task = TaskRepository.parseCsvLine(line, lineNumber);
                if (task != null) {
                    tasks.add(task);
                } else {
                    errors.add(new CsvError(lineNumber, "Ligne mal formée ou erreur de conversion"));
                }

                lineNumber++;
            }
        } catch (IOException e) {
            throw new FileNotExistsException(this.filePath);
        }
        return tasks;
    }

    public ArrayList<TaskCsv> getTasks() throws Exception {
        return this.read();
    }

    public ArrayList<TaskCsv> searchTasks(String fulltext, int maxCount) throws Exception {
        ArrayList<TaskCsv> allTasks = this.read();
        ArrayList<TaskCsv> matchedTasks = new ArrayList<>();

        String normalizedFulltext = StringUtils.normalizeString(fulltext);

        int analysedCount = 0;

        for (TaskCsv task : allTasks) {
            String normalizedDescription = StringUtils.normalizeString(task.getDescription());

            if (normalizedDescription.contains(normalizedFulltext)) {
                matchedTasks.add(task);
                analysedCount++;
            }
            if (analysedCount >= maxCount) {
                break;
            }
        }
        return matchedTasks;

    }

    public void addLineAtEnd(TaskCsv task) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.filePath, true))) {
            writer.write(task.toCsv());
            writer.newLine();
        }
    }

    public void removeLine(int lineNumber) throws Exception {
        File inputFile = new File(this.filePath);
        File tempFile = new File(inputFile.getAbsolutePath() + ".tmp");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String currentLine = reader.readLine();
            writer.write(currentLine);
            writer.newLine();

            boolean found = false;

            int currentLineNumber = 1;
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
                throw new LineNotExistsException(lineNumber);
            }
        }

        if (!inputFile.delete()) {
            throw new DeleteOriginalFileException(this.filePath);
        }
        if (!tempFile.renameTo(inputFile)) {
            throw new RenameTempFileException(tempFile.getAbsolutePath());
        }
    }

    public void updateLine(int lineNumber, String description, boolean completed) throws Exception {
        File inputFile = new File(this.filePath);
        File tempFile = new File(inputFile.getAbsolutePath() + ".tmp");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String currentLine;
            int currentLineNumber = 0;

            while ((currentLine = reader.readLine()) != null) {
                if (currentLineNumber == lineNumber) {
                    TaskCsv newTask = new TaskCsv(lineNumber, description, completed);
                    writer.write(newTask.toCsv());
                } else {
                    writer.write(currentLine);
                }
                writer.newLine();
                currentLineNumber++;
            }
        }

        if (!inputFile.delete()) {
            throw new DeleteOriginalFileException(this.filePath);
        }
        if (!tempFile.renameTo(inputFile)) {
            throw new RenameTempFileException(tempFile.getAbsolutePath());
        }
    }

    public ArrayList<CsvError> getErrors() {
        return errors;
    }

    private static TaskCsv parseCsvLine(String line, int lineNumber) {
        String[] parts = line.split(",");
        if (parts.length != 2) {
            return null;
        }

        String description = parts[0];
        boolean completed = Boolean.parseBoolean(parts[1]);
        return new TaskCsv(lineNumber, description, completed);

    }

}