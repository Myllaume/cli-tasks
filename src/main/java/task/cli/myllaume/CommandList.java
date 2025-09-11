package task.cli.myllaume;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "list", description = "Lister les tâches")
public class CommandList implements Runnable {

    @Option(names = "--oneline", description = "Affiche chaque tâche sur une seule ligne")
    boolean oneline;

    @Override
    public void run() {
        if (oneline) {
            System.out.println("Liste des tâches (une ligne par tâche)...");
        } else {
            System.out.println("Liste des tâches (format classique)...");
        }
    }

}
