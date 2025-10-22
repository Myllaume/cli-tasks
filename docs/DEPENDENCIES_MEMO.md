# 📝 Mémo : Gestion des dépendances en Java

> "Premature optimization is the root of all evil."  
> — Donald Knuth

## 🎯 La question centrale

**Faut-il avoir peur de rendre une classe dépendante de nombreuses autres ?**

Réponse : **Ça dépend !** Il faut distinguer deux cas :

---

## ❌ Mauvais : Dépendre de BEAUCOUP de classes concrètes

### Exemple (God Object)
```java
class GodClass {
    private final MySqlConnection db;
    private final FileLogger logger;
    private final SmtpEmailSender email;
    private final S3FileStorage storage;
    private final StripePaymentProcessor payment;
    private final TwilioSmsService sms;
    private final RedisCache cache;
    private final ElasticsearchClient search;
    private final AwsS3Client s3;
    private final GoogleAnalytics analytics;
    // ❌ 10+ dépendances concrètes = God Object !
    
    public void doEverything() {
        // Fait trop de choses différentes
    }
}
```

**Problèmes :**
- Violation du Single Responsibility Principle
- Difficile à tester (trop de mocks nécessaires)
- Difficile à maintenir (touche à tout)
- Couplage fort avec des implémentations concrètes

---

## ✅ Bon : Dépendre de plusieurs abstractions (orchestration)

### Exemple (Orchestrateur légitime)
```java
class OrderService {
    private final Repository repository;           // Interface
    private final EmailService emailService;       // Interface
    private final PaymentProcessor paymentProc;    // Interface
    private final NotificationService notifier;    // Interface
    // ✅ 4 abstractions = Orchestrateur légitime
    
    public void processOrder(Order order) {
        repository.save(order);
        paymentProc.charge(order.getAmount());
        emailService.sendConfirmation(order.getCustomer());
        notifier.notify(order);
    }
}
```

**Avantages :**
- Orchestration claire et lisible
- Dépend d'abstractions (interfaces), pas d'implémentations
- Facilement testable (mock des interfaces)
- Chaque dépendance a une responsabilité claire

---

## 📊 Règle empirique : Combien de dépendances ?

| Nombre de dépendances | Verdict | Action |
|------------------------|---------|--------|
| **1-3** | ✅ Excellent | Continue comme ça |
| **4-6** | ✅ Bien | Normal pour un orchestrateur |
| **7-9** | ⚠️ Suspect | Revoir la responsabilité de la classe |
| **10+** | ❌ God Object | Refactoring nécessaire ! |

---

## 🔍 Comment évaluer une classe ?

### Checklist de santé :

1. **Nombre de dépendances** : < 7 idéalement
2. **Type de dépendances** : Interfaces > Classes concrètes
3. **Cohésion** : Les dépendances sont-elles liées logiquement ?
4. **Testabilité** : Peut-on facilement mocker les dépendances ?
5. **Clarté** : Le rôle de la classe est-il évident ?

### Exemple d'analyse

```java
// Classe A
class TaskWebServer {
    private final TaskRepositorySqlite repository;  // Concrète ⚠️
    private final TaskHtmlRenderer renderer;        // Pure ✅
}
// 👉 2 dépendances, cohérentes, rôle clair → ✅ Sain

// Classe B
class MegaController {
    private final UserRepo users;
    private final OrderRepo orders;
    private final PaymentService payment;
    private final EmailService email;
    private final SmsService sms;
    private final LogService logs;
    private final CacheService cache;
    private final MetricsService metrics;
    private final ValidationService validator;
}
// 👉 9 dépendances, peu cohérentes → ❌ God Object
```

---

## 💡 Solutions quand ça grossit trop

### 1. Facade Pattern (grouper les dépendances liées)

**Avant** (trop de repositories) :
```java
class TaskWebServer {
    private final TaskRepository taskRepo;
    private final ProjectRepository projectRepo;
    private final TimelogRepository timelogRepo;
    private final TagRepository tagRepo;
    // ⚠️ 4 repositories similaires
}
```

**Après** (façade) :
```java
interface DataProvider {
    List<Task> getTasks(int limit);
    List<Project> getProjects();
    List<Timelog> getTimelogs();
    List<Tag> getTags();
}

class TaskWebServer {
    private final DataProvider dataProvider;  // ✅ 1 seule façade !
    private final HtmlRenderer renderer;
}
```

### 2. Extraire des sous-services

**Avant** (classe fait trop de choses) :
```java
class TaskWebServer {
    // Routing HTTP + Rendering HTML + Data fetching + Static files
    private final TaskRepository repo;
    private final ProjectRepository projects;
    private final TaskHtmlRenderer renderer;
    private final ProjectHtmlRenderer projectRenderer;
    private final FileHandler fileHandler;
    // ⚠️ Trop de responsabilités
}
```

**Après** (séparation) :
```java
class TaskWebServer {
    private final RouteHandler routes;        // ✅ Gère le routing
    private final StaticFileServer files;     // ✅ Gère les fichiers
    // Délègue au lieu de tout faire
}

class RouteHandler {
    private final DataProvider data;
    private final HtmlRenderer renderer;
    // Responsabilité focalisée
}
```

### 3. Dependency Injection Container

Pour les gros projets, utiliser un framework DI :
```java
// Spring Boot
@RestController
public class TaskController {
    @Autowired
    private final TaskService taskService;
    @Autowired
    private final ProjectService projectService;
    // Le container gère l'injection
}
```

---

## 🚨 Signaux d'alerte (Red Flags)

### Ton code a probablement un problème si :

1. ✋ **Plus de 7-8 dépendances** dans le constructeur
2. ✋ **Beaucoup de classes concrètes** au lieu d'interfaces
3. ✋ **Difficulté à nommer la classe** ("Manager", "Handler", "Helper", "Util")
4. ✋ **Tests difficiles** (besoin de 10+ mocks)
5. ✋ **Méthodes longues** (> 20 lignes) qui font beaucoup de choses
6. ✋ **Beaucoup de `if/else`** imbriqués
7. ✋ **Classe > 300 lignes** (sauf cas particuliers)

---

## ✅ Bonnes pratiques

### DO ✅

```java
// ✅ Dépendre d'abstractions
class OrderService {
    private final Repository repo;        // Interface
    private final EmailService email;     // Interface
}

// ✅ Injection par constructeur
public OrderService(Repository repo, EmailService email) {
    this.repo = repo;
    this.email = email;
}

// ✅ Orchestration claire
public void process(Order order) {
    repo.save(order);
    email.send(order.getCustomer());
}
```

### DON'T ❌

```java
// ❌ Créer ses dépendances (couplage fort)
class OrderService {
    private final EmailService email = new SmtpEmailService();
}

// ❌ Dépendre de trop de classes concrètes
class MegaService {
    private final MySqlRepo repo;
    private final AwsS3Client s3;
    private final StripePayment stripe;
    // ... 10 autres classes concrètes
}

// ❌ God Class qui fait tout
class Manager {
    public void doEverything() {
        // 500 lignes de code...
    }
}
```

---

## 📚 Exemples concrets

### Cas 1 : Web Server (simple, sain)
```java
class TaskWebServer implements WebServer {
    private final int port;                        // Primitive
    private final TaskRepositorySqlite repository; // Dépendance
    private final TaskHtmlRenderer renderer;       // Dépendance
    private HttpServer server;                     // Framework
}
// 👉 2 dépendances métier → ✅ Excellent
```

### Cas 2 : Web Server étendu (toujours sain)
```java
class TaskWebServer implements WebServer {
    private final TaskRepositorySqlite taskRepo;
    private final ProjectRepositorySqlite projectRepo;
    private final TaskHtmlRenderer taskRenderer;
    private final ProjectHtmlRenderer projectRenderer;
}
// 👉 4 dépendances métier → ✅ Bien (orchestrateur)
```

### Cas 3 : Web Server complexe (commence à sentir mauvais)
```java
class TaskWebServer implements WebServer {
    private final TaskRepository taskRepo;
    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final TagRepository tagRepo;
    private final TimelogRepository timelogRepo;
    private final TaskRenderer taskRenderer;
    private final ProjectRenderer projectRenderer;
    private final AuthService auth;
    private final CacheService cache;
}
// 👉 9 dépendances → ⚠️ Refactoring recommandé !
```

**Solution** : Créer une façade `DataProvider` pour grouper les repositories.

---

## 🎯 Le principe du "Goldilocks"

> Ni trop peu, ni trop, juste ce qu'il faut !

- **Trop peu de dépendances** (0-1) : La classe fait probablement trop de choses elle-même
- **Nombre raisonnable** (2-6) : ✅ Zone saine pour un orchestrateur
- **Trop de dépendances** (10+) : God Object, refactoring nécessaire

---

## 💬 Citations de sagesse

> "Premature optimization is the root of all evil."  
> — Donald Knuth

👉 N'optimise pas avant d'avoir un problème réel !

> "Make it work, make it right, make it fast."  
> — Kent Beck

👉 Priorité : fonctionnel → propre → performant

> "The best code is no code at all."  
> — Jeff Atwood

👉 Moins de dépendances = moins de complexité

---

## 🎬 Conclusion

### Quand t'inquiéter ?

- ❌ Plus de 7-8 dépendances
- ❌ Beaucoup de classes concrètes
- ❌ Tests difficiles à écrire
- ❌ Classe difficile à expliquer en une phrase

### Quand NE PAS t'inquiéter ?

- ✅ 2-6 dépendances bien justifiées
- ✅ Dépendances sont des interfaces
- ✅ Rôle de la classe est clair
- ✅ Tests simples à écrire

### Règle d'or

**Attends de voir le problème réel avant d'optimiser !**

Si ton code fonctionne, est testable et lisible → C'est bon ! 🎉

---

## 🔗 Liens avec SOLID

| Principe | Rapport avec les dépendances |
|----------|------------------------------|
| **S** Single Responsibility | Trop de dépendances = trop de responsabilités |
| **O** Open/Closed | Dépendre d'interfaces permet l'extension |
| **L** Liskov Substitution | Remplacer les implémentations facilement |
| **I** Interface Segregation | Interfaces petites = moins de dépendances inutiles |
| **D** Dependency Inversion | Dépendre d'abstractions, pas de classes concrètes |

---

## 📖 Ressources

- [Refactoring Guru - Code Smells](https://refactoring.guru/refactoring/smells)
- [Martin Fowler - Refactoring](https://martinfowler.com/books/refactoring.html)
- [Clean Code - Robert C. Martin](https://www.amazon.com/Clean-Code-Handbook-Software-Craftsmanship/dp/0132350882)
