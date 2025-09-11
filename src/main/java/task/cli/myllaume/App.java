package task.cli.myllaume;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "tasks", mixinStandardHelpOptions = true, description = "Gestion des tâches en CLI", subcommands = {
        CommandList.class,
})
public class App implements Runnable {
    @Override
    public void run() {
        // Peut rester vide si tout passe par les sous-commandes
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}
