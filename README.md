# CharityMap

CharityMap est une application logicielle d'analyse et de partitionnement spatial 2D basée sur les diagrammes de **Voronoï** et la triangulation de **Delaunay**. Elle est conçue pour optimiser et cartographier la distribution de l'aide caritative (nourriture, soins, vêtements) sur un territoire donné.

L'application permet aux ONG et associations de visualiser dynamiquement leurs zones de couvertures respectives, d'importer des points de distribution et de simuler la répartition des bénéficiaires.

## Fonctionnalités

- **Interface Graphique Interactive** : Visualisation cartographique avec zoom, panoramique (pan), et interactions avec les nœuds.
- **Mathématiques Spatiales** : Calcul et affichage en temps réel de la triangulation de Delaunay et des diagrammes de Voronoï.
- **Gestion Multicouche** : Construction de réseaux de Voronoï indépendants selon le type de besoin (Alimentaire, Vestimentaire, Médical).
- **Import/Export** : Chargement de centres de distribution depuis des fichiers CSV et persistance binaire de la carte (fichiers `.map`).
- **Génération Aléatoire** : Outil de simulation générant dynamiquement des milliers de bénéficiaires dans une bounding box spécifique.
- **Statistiques en Temps Réel** : Évaluation de la charge de chaque centre d'aide (nombre de bénéficiaires assignés, distance moyenne de trajet, etc.).

## Prérequis

- **Java 17** (ou supérieur)
- *Recommandé :* **Maven 3.8+** (pour simplifier la gestion de JavaFX)

## Installation et Lancement

Clonez le dépôt sur votre machine locale :
```bash
git clone https://github.com/Fufulino/Genie-Logiciel.git
cd Genie-Logiciel
```

### Méthode 1 : Avec Maven (Recommandée)

C'est la méthode la plus simple car Maven s'occupe de télécharger automatiquement JavaFX et de tout configurer.

- Lancer l'interface graphique : `mvn clean compile javafx:run`
- Lancer la version console (CLI) : `mvn exec:java -Dexec.mainClass="com.charitymap.cli.CommandLineApp"`

### Méthode 2 : Depuis un IDE (IntelliJ IDEA, Eclipse, VS Code)

Vous pouvez tout à fait utiliser le projet sans aucune ligne de commande :
1. Ouvrez/Importez le dossier du projet dans votre IDE préféré.
2. L'IDE détectera automatiquement le fichier `pom.xml` et téléchargera les bibliothèques.
3. Exécutez la classe `src/main/java/com/charitymap/gui/CharityMapApp.java` (en cliquant sur la flèche verte ou "Run").

### Méthode 3 : Sans Maven du tout (Version CLI Uniquement)

Si vous n'avez ni Maven ni un IDE, vous pouvez toujours compiler et lancer la version Console (qui n'a pas besoin de JavaFX) de manière standard avec le compilateur Java :

```bash
# Compiler tous les fichiers Java dans un dossier "out"
javac -d out $(find src/main/java -name "*.java")

# Lancer le programme Console
java -cp out com.charitymap.cli.CommandLineApp
```

### Génération de la Documentation (Javadoc)
Pour générer la documentation technique complète du code :
```bash
mvn javadoc:javadoc
```
Les fichiers HTML seront générés dans le dossier `target/site/apidocs/`. Vous pourrez alors ouvrir le fichier `index.html` dans votre navigateur.

## Architecture du Code

Le projet est construit sur une architecture en couches (Clean Architecture) garantissant que le moteur mathématique est totalement indépendant de la technologie d'affichage :

- `com.charitymap.model` : Entités métier (Centres, Bénéficiaires, Types d'aide).
- `com.charitymap.geometry` : Cœur algorithmique (Bowyer-Watson, constructeur Voronoï).
- `com.charitymap.service` : Gestion des E/S (Imports CSV, persistance de la carte, générateurs de données).
- `com.charitymap.gui` : Interface utilisateur basée sur JavaFX.
- `com.charitymap.cli` : Interface textuelle (Console).

## Utilisation

1. **Ajout de centres** : Via le panneau latéral ou par l'import d'un fichier CSV (ex: `exemple_centres.csv`).
2. **Génération de bénéficiaires** : Remplissez la carte avec des utilisateurs générés aléatoirement pour tester les zones de couverture.
3. **Analyse visuelle** : Activez ou désactivez les calques (Delaunay, Voronoï, liens d'assignation) pour analyser comment l'espace est découpé entre les différentes associations.
4. **Interaction** : Cliquez sur un centre ou un bénéficiaire pour voir ses métriques détaillées (distance de trajet, surface de couverture).
