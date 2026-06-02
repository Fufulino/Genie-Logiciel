# CharityMap — version ligne de commande

Partitionnement spatial 2D (Voronoï / Delaunay) au service des
associations caritatives de Cergy.

## Compiler et lancer

Depuis le dossier `charitymap` :

```
javac -d out $(find src -name "*.java")
java -cp out com.cergy.charitymap.cli.CommandLineApp
```

Le jour de la soutenance, le barème impose de récupérer le dépôt Git,
de compiler en ligne de commande, et de lancer le projet — sans passer
par un environnement de développement. Les deux commandes ci-dessus
suffisent. Entraînez-vous à les taper.

## Organisation des packages (architecture en couches)

- `model`     : entités métier pures, AUCUN import JavaFX.
                Point, AidType, Association, DistributionCenter,
                Beneficiary, Distributor, CharityMap (le chef
                d'orchestre).
- `geometry`  : les mathématiques. GeometryUtils, Triangle,
                DelaunayTriangulator (Bowyer-Watson), VoronoiBuilder,
                VoronoiCell.
- `service`   : entrées/sorties. RandomGenerator (ajout en masse
                aléatoire), MapIO (export/import binaire).
- `cli`       : CommandLineApp, l'interface en ligne de commande.

Règle d'or : les dépendances descendent toujours (cli -> service ->
geometry -> model). Le modèle ne sait jamais que JavaFX existe. C'est
ce qui rendra l'ajout de JavaFX simple : il suffira d'ajouter un
package `view` qui appelle CharityMap, sans toucher au reste.

## Le mapping métier (volet Éthique/Design)

- Site Voronoï        = DistributionCenter (local d'une association)
- Cellule Voronoï     = zone de couverture d'un local
- Point utilisateur   = Beneficiary (personne aidée)
- Type d'aide         = AidType { CLOTHING, FOOD, CARE }

Comme chaque local fournit UN seul type d'aide, on construit UN
diagramme de Voronoï PAR type d'aide. Un bénéficiaire est rattaché au
local le plus proche FOURNISSANT LE BON TYPE d'aide.

## Deux points à savoir expliquer en soutenance

1. Cellules ouvertes. Les cellules de Voronoï au bord du diagramme
   sont non bornées (elles s'étendent à l'infini). Leur surface
   affichée vaut donc 0.00 : ce n'est pas un bug, c'est mathématique.
   Piste d'amélioration : couper les cellules sur un rectangle
   englobant (la "bounding box" de Cergy) pour fermer les polygones.
   C'est un excellent sujet à présenter comme limite identifiée.

2. Bowyer-Watson. La triangulation de Delaunay est construite en
   insérant les points un par un, en retirant les triangles dont le
   cercle circonscrit contient le nouveau point, puis en re-triangulant
   le trou. Voronoï s'en déduit par dualité : les sommets de Voronoï
   sont les centres des cercles circonscrits de Delaunay. Chaque membre
   du groupe doit savoir réexpliquer ce paragraphe avec ses mots.

## Prochaines étapes suggérées

- Fermer les cellules sur une bounding box pour des surfaces réelles.
- Statistiques de déséquilibre via les triangles de Delaunay
  (différence de bénéficiaires entre deux locaux voisins).
- Génération de la JavaDoc : `javadoc -d doc $(find src -name "*.java")`
- Puis seulement, la couche JavaFX (package `view`).
