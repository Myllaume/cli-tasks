package task.cli.myllaume;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "tasks", mixinStandardHelpOptions = true, description = "Gestion des tâches en CLI")
public class App implements Runnable {
    @Override
    public void run() {
        // Peut rester vide si tout passe par les sous-commandes
    }

    public static void main(String[] args) {
        String dataDir = new AppDirs().getDataDir();
        String filePath = dataDir + "/tasks.csv";
        TaskRepository repo = new TaskRepository(filePath);

        repo.init(false);

        CommandList commandList = new CommandList(repo);

        CommandLine cmd = new CommandLine(new App());
        cmd.addSubcommand("list", commandList);

        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }
}
