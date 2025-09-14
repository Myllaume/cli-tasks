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

        CommandLine cmd = new CommandLine(new App());

        CommandList commandList = new CommandList(repo);
        cmd.addSubcommand("list", commandList);
        CommandAdd commandAdd = new CommandAdd(repo);
        cmd.addSubcommand("add", commandAdd);
        CommandRemove commandRemove = new CommandRemove(repo);
        cmd.addSubcommand("remove", commandRemove);

        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }
}
