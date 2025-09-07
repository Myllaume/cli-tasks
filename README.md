# Tasks CLI

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
