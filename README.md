# Tasks CLI

## To Do

- Ajouter à la configuration du logiciel
  - Format de date préféré
  - Fuseau horaire
  - Langue/locale
  - Couleurs pour les priorités
  - Nombre maximum de tâches à afficher par défaut
  - Priorité par défaut pour les nouvelles tâches


## Changelog

### Initialisation

J'ai initialisé ce projet avec la commande suviante.

```bash
mvn archetype:generate -DgroupId=task.cli.myllaume -DartifactId=java-cli-tasks -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```

### Mise à jour dépendance JUnit

J'ai ensuite modifié la version de la dépendance JUnit depuis le fichier ./pom.xml.

```diff
  <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
-     <version>3.8.1</version>
+     <version>4.13.2</version>
      <scope>test</scope>
  </dependency>
```

Puis j'ai executé la commande suivante.

```bash
mvn clean install
```

### Execution des tests

J'ai executé des tests avec JUnit, par l'intermédiaire de Maven, avec la commande suivante.

```bash
mvn test
```

### Executer les tests et compiler le code

1. clean : Supprime le dossier target (où Maven met les fichiers compilés et les anciens artefacts), pour repartir sur une base propre.
2. package : Compile le code source, exécute les tests, puis crée le fichier JAR (ou WAR) de ton projet dans le dossier target.

```bash
mvn clean package
```

### Utiliser le CLI

```bash
cd target
java -jar java-cli-tasks-1.0-SNAPSHOT.jar --help
java -jar java-cli-tasks-1.0-SNAPSHOT.jar --version
```