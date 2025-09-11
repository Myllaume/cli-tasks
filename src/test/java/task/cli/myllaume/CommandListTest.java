package task.cli.myllaume;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class CommandListTest {

    @Test
    public void testRunOnelineTrue() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;

        try {
            System.setErr(new PrintStream(err)); // setup
            System.setOut(new PrintStream(out));

            CommandList cmd = new CommandList();
            cmd.oneline = true;
            cmd.run();
        } finally {
            System.setErr(oldErr);
            System.setOut(oldOut);
        }

        assertTrue(out.toString().contains("une ligne par tâche"));
        assertEquals("", err.toString());
    }

    @Test
    public void testRunOnelineFalse() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;

        try {
            System.setErr(new PrintStream(err)); // setup
            System.setOut(new PrintStream(out));

            CommandList cmd = new CommandList();
            cmd.oneline = false;
            cmd.run();
        } finally {
            System.setErr(oldErr);
            System.setOut(oldOut);
        }

        assertTrue(out.toString().contains("format classique"));
        assertEquals("", err.toString());
    }

}