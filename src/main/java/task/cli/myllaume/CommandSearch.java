package task.cli.myllaume;

import java.util.ArrayList;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "search", description = "Rechercher une tâche")
public class CommandSearch implements Runnable {
    private final TaskRepository repo;

    public CommandSearch(TaskRepository repo) {
        this.repo = repo;
    }

    @Parameters(index = "0", description = "Chaîne de recherche")
    String fulltext;

    @Option(names = { "-m", "--max-results" }, description = "Nombre maximum de résultats", defaultValue = "5")
    int maxResults;

    @Option(names = { "-c", "--max-count" }, description = "Nombre maximum de lignes analysées", defaultValue = "100")
    int maxCount;

    @Override
    public void run() {
        if (fulltext.trim().isEmpty()) {
            System.out.println("Aucune chaîne de recherche fournie.");
            return;
        }

        if (maxCount < maxResults) {
            System.out.println(
                    "Le nombre maximum de résultats affiché ne peut pas être inférieur au nombre maximum de résultats à analyser.");
            return;
        }

        ArrayList<Task> tasks = repo.searchTasks(fulltext, maxCount);

        tasks.stream()
                .limit(maxResults)
                .forEach(task -> System.out.println(task.toString()));

        System.out.println(
                "Recherche terminée. Affichage de " + Math.min(maxResults, tasks.size()) + " résultats sur "
                        + tasks.size() + " trouvés.");
    }

}
