# 📝 Mémo : Quand créer des interfaces en Java ?

## 🎯 Principe SOLID "D" (Dependency Inversion)

> **"Dépendre d'abstractions (interfaces), pas d'implémentations concrètes"**

---

## ✅ Quand créer une interface ?

### 1. Dépendances externes difficiles à tester

Toute interaction avec le monde extérieur : système de fichiers, réseau, APIs système, etc.

```java
// ✅ Interface recommandée
interface BrowserLauncher {
    void openUrl(String url);
}

class DesktopBrowserLauncher implements BrowserLauncher {
    @Override
    public void openUrl(String url) {
        Desktop.getDesktop().browse(new URI(url)); // API système
    }
}

// En test : on peut créer un FakeBrowserLauncher
class FakeBrowserLauncher implements BrowserLauncher {
    @Override
    public void openUrl(String url) {
        // Ne fait rien, parfait pour les tests !
    }
}
```

**Autres exemples :**
- Base de données → `interface TaskRepository`
- API HTTP → `interface HttpClient`
- Envoi email → `interface EmailService`
- Fichiers → `interface FileStorage`

### 2. Logique métier avec plusieurs implémentations possibles

Quand tu peux légitimement avoir différentes façons de faire la même chose.

```java
// ✅ Interface recommandée
interface NotificationService {
    void send(String message);
}

// Plusieurs implémentations légitimes
class EmailNotification implements NotificationService { ... }
class SlackNotification implements NotificationService { ... }
class SmsNotification implements NotificationService { ... }
```

### 3. Boundaries entre couches architecturales

Pour isoler la logique métier de l'infrastructure.

```java
// ✅ Interface recommandée
interface WebServer {
    void start() throws IOException;
    void stop(int delaySeconds);
    String getUrl();
}

// L'implémentation peut changer (HttpServer, Jetty, Tomcat...)
class TaskWebServer implements WebServer { ... }
```

---

## ❌ Quand NE PAS créer d'interface ?

### 1. Classes "stupides" (POJOs / DTOs)

Simple transport de données, pas de logique.

```java
// ❌ PAS d'interface ici !
class Task {
    private String description;
    private TaskPriority priority;
    private LocalDateTime createdAt;
    
    // Juste des getters/setters
    public String getDescription() { return description; }
    public void setDescription(String desc) { this.description = desc; }
}
```

**Pourquoi ?** Pas de logique à mocker, juste des données.

### 2. Utilitaires purs (sans état ni effet de bord)

Fonctions pures, déterministes, faciles à tester.

```java
// ❌ PAS d'interface ici !
class StringUtils {
    static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }
}
```

**Pourquoi ?** Pas d'effet de bord, déjà facile à tester.

### 3. Logique unique et stable

Une seule façon de faire, pas de variation prévue.

```java
// ❌ PAS d'interface ici !
class TaskHtmlRenderer {
    String renderHome(List<Task> tasks) {
        StringBuilder html = new StringBuilder();
        for (Task task : tasks) {
            html.append("<li>").append(task.getDescription()).append("</li>");
        }
        return html.toString();
    }
}
```

**Pourquoi ?** Logique pure, pas de dépendance externe, facile à tester directement.

### 4. Points d'entrée / Orchestrateurs

Classes qui coordonnent mais n'ont qu'une seule implémentation logique.

```java
// ❌ PAS d'interface ici !
@Command(name = "gui")
class CommandGui implements Runnable {
    private final WebServer server;        // ✅ Dépend d'interface
    private final BrowserLauncher browser; // ✅ Dépend d'interface
    
    @Override
    public void run() {
        server.start();
        browser.openUrl(server.getUrl());
    }
}
```

**Pourquoi ?** C'est le point d'entrée, une seule implémentation suffit. Ses *dépendances* ont des interfaces.

---

## 🎯 Règle d'or

### Si tu te poses la question :

```
Puis-je tester cette classe facilement sans ses dépendances réelles ?
```

- **OUI** → Pas besoin d'interface
- **NON** → Crée une interface !

### Exemples concrets :

| Classe | Testable sans interface ? | Interface ? |
|--------|--------------------------|-------------|
| `StringUtils.escapeHtml()` | ✅ Oui (fonction pure) | ❌ Non |
| `Task` (POJO) | ✅ Oui (pas de logique) | ❌ Non |
| `TaskHtmlRenderer` | ✅ Oui (logique pure) | ❌ Non |
| `DesktopBrowserLauncher` | ❌ Non (ouvre vraiment le navigateur) | ✅ Oui |
| `TaskWebServer` | ❌ Non (démarre vraiment un serveur) | ✅ Oui |
| `TaskRepository` | ❌ Non (accès base de données) | ✅ Oui |

---

## 💡 Avantages des interfaces

### 1. Testabilité
```java
// Sans interface
@Test
void test() {
    DesktopBrowserLauncher browser = new DesktopBrowserLauncher();
    // ❌ Va vraiment ouvrir le navigateur !
}

// Avec interface
@Test
void test() {
    BrowserLauncher fakeBrowser = new FakeBrowserLauncher();
    CommandGui cmd = new CommandGui(repo, factory, fakeBrowser);
    // ✅ Pas d'ouverture de navigateur dans les tests !
}
```

### 2. Flexibilité
```java
// Tu peux changer l'implémentation sans toucher au code client
BrowserLauncher browser = new ChromeBrowserLauncher();
// ou
BrowserLauncher browser = new FirefoxBrowserLauncher();
// ou
BrowserLauncher browser = new MockBrowserLauncher();

// CommandGui ne change JAMAIS !
```

### 3. Découplage
```java
// CommandGui ne connaît PAS DesktopBrowserLauncher
// Il connaît juste le contrat : "il y a une méthode openUrl()"
public class CommandGui {
    private final BrowserLauncher browser; // Abstraction !
}
```

---

## 📚 En résumé

✅ **Crée des interfaces pour :**
- Dépendances externes (fichiers, réseau, API système)
- Logique avec plusieurs implémentations possibles
- Boundaries entre couches

❌ **Ne crée PAS d'interface pour :**
- POJOs / DTOs (classes de données)
- Utilitaires statiques purs
- Logique unique et stable sans dépendances externes

🎯 **Sois pragmatique, pas dogmatique !**

---

## 🔗 Liens avec SOLID

| Principe | Rapport avec les interfaces |
|----------|----------------------------|
| **S** Single Responsibility | Une interface = un seul contrat |
| **O** Open/Closed | Extensible via nouvelles implémentations |
| **L** Liskov Substitution | Toutes les implémentations respectent le contrat |
| **I** Interface Segregation | Interfaces petites et ciblées |
| **D** Dependency Inversion | Dépendre d'abstractions, pas de classes concrètes |
