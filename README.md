# Smart Energy Manager 🔋

Application graphique de gestion énergétique pour bâtiments - Projet ISEP 2025-2028

## 📋 Fonctionnalités

### ✅ Gestion des Bâtiments
- Créer, modifier et supprimer des bâtiments
- Support de 6 types de bâtiments :
  - Maison
  - Appartement
  - Bureau
  - Local commercial
  - Bâtiment universitaire
  - Autre structure
- Affichage détaillé des caractéristiques

### ⚡ Suivi Énergétique
- Enregistrement des consommations énergétiques
- Types d'énergie :
  - Électricité
  - Chauffage/Gaz
  - Eau
  - Éclairage
  - Climatisation
  - Énergies renouvelables
- Statistiques : total, moyenne, min, max
- Visualisation par bâtiment

### 💾 Persistance
- Base de données SQLite
- Sauvegarde automatique
- Import/Export de données de test

## 🛠 Technologie

- **Langage** : Java 17+
- **Interface** : Swing (javax.swing)
- **BD** : SQLite 3
- **Build** : Maven
- **IDE** : IntelliJ IDEA / Eclipse / VS Code

## 📦 Installation

### Prérequis
- Java 17+ installé
- Maven 3.6+ (optionnel - IntelliJ peut compiler directement)

### Compiler
```bash
mvn clean compile
```

### Exécuter
```bash
java -cp target/classes Main
```

## 🏗 Architecture

### Classes Principales
```
Batiment (abstract)
├── Maison
├── Appartement
├── Bureau
├── Local_commercial
├── Batiment_Universitaire
└── Autre_Structure

Consommation_Energie
TypeConsommation (enum)

DatabaseManager (SQLite)
InterfaceGraphique (JFrame)
Main
```

### Schéma BD
```
Table: batiments
├── id (PRIMARY KEY)
├── nom
├── type
├── nombre_etages
└── details

Table: consommations
├── id (PRIMARY KEY)
├── batiment_id (FK)
├── type (enum)
└── quantite
```

## 👥 Guide Utilisateur

1. **Ajouter un bâtiment** : Cliquer "Ajouter" → Remplir les infos → OK
2. **Ajouter une consommation** : Sélectionner un bâtiment → "Ajouter" → Choisir type/quantité
3. **Voir les détails** : Sélectionner un bâtiment → "Détails"
4. **Afficher stats** : Sélectionner un bâtiment → "Statistiques"
5. **Test** : Cliquer "Charger Test" pour données d'exemple

## 🔧 Corrections Apportées

✅ Réécriture complète de InterfaceGraphique.java
✅ Ajout de `getAllBatiments()` dans DatabaseManager
✅ Création du pom.xml avec dépendances SQLite
✅ Amélioration du .gitignore
✅ Gestion correcte des types de données
✅ Code nettoyé et professionnel
✅ Aucune erreur de compilation

## 📝 Notes

- Code modulaire et maintenable
- Design pattern MVC simplifié
- Toutes les données persistées
- Interface intuitive et responsive