package task.cli.myllaume;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import task.cli.myllaume.config.AppConfigRepository;
import task.cli.myllaume.db.ProjectsRepository;
import task.cli.myllaume.db.TaskManager;

@Command(
    name = "tasks",
    mixinStandardHelpOptions = true,
    description = "Gestion des tâches en CLI",
    version = "0.1.0-alpha.1")
public class App implements Runnable {
  @Override
  public void run() {
    // Peut rester vide si tout passe par les sous-commandes
  }

  public static void main(String[] args) {
    try {

      AppDirs appDirs = new AppDirs();
      String dataDir = appDirs.getDataDir();
      String configDir = appDirs.getConfigDir();

      ProjectsRepository projectsRepository = new ProjectsRepository(dataDir);
      AppState appState = new AppState(new AppConfigRepository(configDir), projectsRepository);

      try {
        if (appState.isFirstLaunch()) {
          appState.firstLaunchSetup();
          System.out.println("✓ Initial configuration completed successfully");
        }
      } catch (Exception e) {
        System.err.println("Critical error during first launch: " + e.getMessage());
        System.err.println("Please check write permissions in: " + dataDir);
        System.exit(1);
      }

      TaskRepositorySqlite tasksRepo = new TaskRepositorySqlite(dataDir);

      TaskManager manager = new TaskManager(tasksRepo, projectsRepository);

      CommandLine cmd = new CommandLine(new App());
      CommandList commandList = new CommandList(tasksRepo);
      cmd.addSubcommand("list", commandList);
      CommandAdd commandAdd = new CommandAdd(manager);
      cmd.addSubcommand("add", commandAdd);
      CommandSearch commandSearch = new CommandSearch(tasksRepo);
      cmd.addSubcommand("search", commandSearch);
      CommandRemove commandRemove = new CommandRemove(tasksRepo);
      cmd.addSubcommand("remove", commandRemove);
      CommandDone commandDone = new CommandDone(tasksRepo);
      cmd.addSubcommand("done", commandDone);
      CommandImport commandImport = new CommandImport(manager);
      cmd.addSubcommand("import", commandImport);

      int exitCode = cmd.execute(args);
      System.exit(exitCode);

    } catch (Exception e) {
      System.err.println("Unexpected error: " + e.getMessage());
      e.printStackTrace(System.err);
      System.exit(1);
    }
  }
}
