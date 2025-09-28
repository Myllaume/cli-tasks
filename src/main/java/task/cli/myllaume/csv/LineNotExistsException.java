package task.cli.myllaume.csv;

import java.io.IOException;

public class LineNotExistsException extends IOException {
    private final int lineNumber;
    
    public LineNotExistsException(int lineNumber) {
        super("La ligne n°" + lineNumber + " n'existe pas.");
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
