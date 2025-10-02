package task.cli.myllaume.csv;

import java.util.ArrayList;

public class CsvParsingException extends Exception {
    private final ArrayList<CsvError> errors;

    public CsvParsingException(ArrayList<CsvError> errors) {
        super("Erreurs de parsing CSV détectées");
        this.errors = errors;
    }

    public ArrayList<CsvError> getErrors() {
        return errors;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Erreurs de parsing CSV détectées:\n");
        for (CsvError error : errors) {
            sb.append("  Ligne ").append(error.getLineNumber()).append(": ").append(error.getMessage()).append("\n");
        }
        return sb.toString();
    }
}
