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
        String configDir = new AppDirs().getConfigDir();
        AppConfigRepository appConfig = new AppConfigRepository(configDir);
        try {
            appConfig.init();
        } catch (Exception e) {
            System.out.println("Erreur lors de l'initialisation de la configuration : " + e.getMessage());
        }

        String dataDir = new AppDirs().getDataDir();
        TaskRepositorySqlite repo = new TaskRepositorySqlite(dataDir);

        CommandLine cmd = new CommandLine(new App());

        try {
            repo.init();
        } catch (Exception e) {
            System.out.println("Erreur lors de l'initialisation de la base de données : " + e.getMessage());
        }

        CommandList commandList = new CommandList(repo);
        cmd.addSubcommand("list", commandList);
        CommandAdd commandAdd = new CommandAdd(repo);
        cmd.addSubcommand("add", commandAdd);
        CommandSearch commandSearch = new CommandSearch(repo);
        cmd.addSubcommand("search", commandSearch);
        CommandRemove commandRemove = new CommandRemove(repo);
        cmd.addSubcommand("remove", commandRemove);
        CommandDone commandDone = new CommandDone(repo);
        cmd.addSubcommand("done", commandDone);

        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }
}
