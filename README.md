<h1 align="center">🎨 ArtConnect — Java / MySQL Art Community Platform</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/JavaFX-1F6FEB?style=for-the-badge&logo=java&logoColor=white"/>
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white"/>
  <img src="https://img.shields.io/badge/JDBC-F80000?style=for-the-badge&logo=oracle&logoColor=white"/>
  <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white"/>
</p>

<p align="center">
  <b>A desktop application connecting local artists, artworks and cultural events — backed by a normalized MySQL database with a full JDBC persistence layer.</b>
</p>

---

## 🇫🇷 Présentation (Français)

**ArtConnect** est une application de bureau (JavaFX) pour une initiative citoyenne valorisant la scène artistique locale. Elle permet de mettre en relation des **artistes**, de présenter leurs **œuvres**, d'organiser des **événements** (expositions, ateliers) et de laisser la communauté les découvrir et s'y inscrire.

Le cœur du projet est la **conception d'une base de données relationnelle normalisée** et son intégration à une application Java existante, en remplaçant une couche de données « en mémoire » par une **vraie couche de persistance JDBC**.

### Conception de la base de données

- **Modélisation** — MCD/MLD conçus à partir du besoin métier, **normalisés jusqu'à la 3ᵉ forme normale (3FN)**
- **Schéma MySQL** — tables avec clés primaires/étrangères, contraintes (`NOT NULL`, `UNIQUE`), relations
- **Vues** — ex. œuvres disponibles à la vente (jointures artistes/galeries)
- **Triggers & procédures stockées** — plus de 500 lignes : contrôles de cohérence (dates, places), audit des modifications, opérations métier automatisées
- **Transactions** — scénarios transactionnels atomiques (inscriptions multiples, opérations couplées)

### Architecture logicielle (couches)

Le projet suit une **architecture en couches** stricte :

- **`model/`** — entités métier (Artist, Artwork, Exhibition, Gallery, Workshop, CommunityMember, Booking, Review…)
- **`dao/`** — interfaces d'accès aux données (contrats)
- **`persistence/`** — implémentations **JDBC** des DAO (`PreparedStatement`, gestion des ressources et transactions)
- **`service/`** — couche métier, avec **deux implémentations** : `InMemory*` (pour les tests) et `Jdbc*` (connectée à la base)
- **`ui/`** — contrôleurs JavaFX + vues **FXML** (onglets Artists, Artworks, Exhibitions, Galleries, Workshops, Community, Discover)
- **`config/` & `util/`** — configuration de connexion, `ConnectionManager`, diagnostic de base

---

## 🇬🇧 Overview (English)

**ArtConnect** is a JavaFX desktop application for a civic initiative promoting the local art scene. It connects **artists**, showcases their **artworks**, organizes **events** (exhibitions, workshops) and lets the community discover and register for them.

The core of the project is the **design of a normalized relational database** and its integration into an existing Java application, replacing an in-memory data layer with a **real JDBC persistence layer**.

### Database design

- **Modeling** — conceptual/logical models built from business needs, **normalized to Third Normal Form (3NF)**
- **MySQL schema** — tables with primary/foreign keys, constraints (`NOT NULL`, `UNIQUE`), relationships
- **Views** — e.g. artworks available for sale (artist/gallery joins)
- **Triggers & stored procedures** — 500+ lines: consistency checks (dates, seats), change auditing, automated business operations
- **Transactions** — atomic transactional scenarios (multiple registrations, coupled operations)

### Software architecture (layers)

The project follows a strict **layered architecture**:

- **`model/`** — business entities (Artist, Artwork, Exhibition, Gallery, Workshop, CommunityMember, Booking, Review…)
- **`dao/`** — data-access interfaces (contracts)
- **`persistence/`** — **JDBC** DAO implementations (`PreparedStatement`, resource & transaction handling)
- **`service/`** — business layer, with **two implementations**: `InMemory*` (for testing) and `Jdbc*` (database-connected)
- **`ui/`** — JavaFX controllers + **FXML** views (Artists, Artworks, Exhibitions, Galleries, Workshops, Community, Discover tabs)
- **`config/` & `util/`** — connection configuration, `ConnectionManager`, database diagnostics

---

## 📁 Structure

```
artconnect/
├── src/main/java/com/project/artconnect/
│   ├── model/          # Entités métier / business entities
│   ├── dao/            # Interfaces DAO
│   ├── persistence/    # Implémentations JDBC / JDBC implementations
│   ├── service/        # Couche métier (InMemory + JDBC)
│   ├── ui/             # Contrôleurs JavaFX / JavaFX controllers
│   ├── config/         # Configuration base de données
│   └── util/           # ConnectionManager, diagnostics
├── src/main/resources/com/project/artconnect/ui/
│   └── *.fxml          # Vues JavaFX / JavaFX views
├── SQL/
│   ├── Art-connect_bdd.sql              # Schéma / schema
│   ├── Insert.sql                       # Données d'exemple / sample data
│   ├── Rest.sql                         # Vues / views
│   ├── artconnect_t3_triggers_procs.sql # Triggers & procédures (500+ lignes)
│   └── artconnect_t4_transactions.sql   # Scénarios transactionnels
├── database.properties.sample           # Modèle de config / config template
├── pom.xml
└── docs/                                # Sujet du projet / project brief
```

---

## 🚀 Installation & Usage

```bash
git clone https://github.com/Manoel24074/artconnect.git
cd artconnect
```

**1. Base de données / Database** — dans MySQL, exécuter les scripts dans l'ordre :
```sql
SOURCE SQL/Art-connect_bdd.sql;
SOURCE SQL/Insert.sql;
SOURCE SQL/Rest.sql;
SOURCE SQL/artconnect_t3_triggers_procs.sql;
SOURCE SQL/artconnect_t4_transactions.sql;
```

**2. Configuration** — copier le modèle et renseigner vos identifiants :
```bash
cp database.properties.sample src/main/resources/database.properties
# éditer database.properties avec votre mot de passe MySQL
```

**3. Compiler & lancer / Build & run :**
```bash
mvn clean javafx:run
```

> ⚠️ **Sécurité / Security :** Le mot de passe réel n'est **jamais** committé. Le fichier `database.properties` est dans le `.gitignore` ; seul le modèle `.sample` (avec un placeholder) est versionné.
> *The real password is **never** committed. `database.properties` is git-ignored; only the `.sample` template (with a placeholder) is versioned.*

---

## 🧰 Tech Stack

`Java` · `JavaFX` · `FXML` · `MySQL` · `JDBC` · `Maven` · layered architecture (Model / DAO / Service / UI)

---

## 🎓 Context

Projet réalisé pour le cours de **Bases de Données 2** (TI603) à **EFREI Paris** (INGE1).
*Project completed for the **Databases 2** course (TI603) at **EFREI Paris** (INGE1).*

---

<p align="center">
  <a href="https://github.com/Manoel24074">← Back to profile / Retour au profil</a>
</p>
