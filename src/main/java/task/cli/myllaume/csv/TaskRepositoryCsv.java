package task.cli.myllaume.csv;

import java.util.ArrayList;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class TaskRepositoryCsv {
    private final String filePath;
    static private final String header = "description,completed";

    private TaskRepositoryCsv(String filePath) {
        this.filePath = filePath;
    }

    public static TaskRepositoryCsv of(String filePath) {
        File file = new File(filePath);
        if (!file.getName().endsWith(".csv")) {
            throw new IllegalArgumentException("Le fichier doit avoir l'extension .csv");
        }

        return new TaskRepositoryCsv(file.getAbsolutePath());
    }

    public void init(boolean overwrite) throws IOException {
        File file = new File(this.filePath);
        if (!file.exists() || overwrite) {
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.write(TaskRepositoryCsv.header);
                writer.newLine();
            }
        }
    }

    private ArrayList<TaskCsv> read() throws Exception {
        File file = new File(this.filePath);
        if (!file.exists()) {
            throw new FileNotExistsException(this.filePath);
        }

        ArrayList<TaskCsv> tasks = new ArrayList<>();
        ArrayList<CsvError> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(this.filePath), StandardCharsets.UTF_8))) {

            int lineNumber = 1;

            String header = reader.readLine();
            if (!header.equals(TaskRepositoryCsv.header)) {
                errors.add(new CsvError(1, "Format d'en-tête incorrect."));
                throw new CsvParsingException(errors);
            }

            String line;

            lineNumber++;

            while ((line = reader.readLine()) != null) {
                try {
                    TaskCsv task = TaskRepositoryCsv.parseCsvLine(line, lineNumber);
                    tasks.add(task);
                } catch (Exception e) {
                    errors.add(new CsvError(lineNumber, e.getMessage()));
                }

                lineNumber++;
            }
        }

        if (!errors.isEmpty()) {
            throw new CsvParsingException(errors);
        }

        return tasks;
    }

    public ArrayList<TaskCsv> getTasks() throws Exception {
        return this.read();
    }

    public void addLineAtEnd(TaskCsv task) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(this.filePath, true), StandardCharsets.UTF_8))) {
            writer.write(task.toCsv());
            writer.newLine();
        }
    }

    private static TaskCsv parseCsvLine(String line, int lineNumber) throws Exception {
        String[] parts = line.split(",");
        if (parts.length != header.split(",").length) {
            throw new Exception("Ligne mal formée ou erreur de conversion.");
        }

        if (parts[0].trim().isEmpty()) {
            throw new Exception("Le format du champ 'description' est incorrect.");
        }

        if (!parts[1].equals("true") && !parts[1].equals("false")) {
            throw new Exception("Le format du champ 'completed' est incorrect.");
        }

        String description = parts[0];
        boolean completed = Boolean.parseBoolean(parts[1]);
        return new TaskCsv(lineNumber, description, completed);
    }

}