# Rapport de Projet - L4M Esports
## Plateforme de Gestion de Tournois Esports

---

## Table des matières

1. [Description du projet](#1-description-du-projet)
2. [Analyse et conception](#2-analyse-et-conception)
3. [Architecture](#3-architecture)
4. [Schéma de la base de données](#4-schéma-de-la-base-de-données)
5. [Mise en œuvre](#5-mise-en-œuvre)
6. [Présentation du résultat](#6-présentation-du-résultat)
7. [Conclusion et perspectives](#7-conclusion-et-perspectives)

---

## 1. Description du projet

### 1.1 Vue d'ensemble

**L4M Esports** est une plateforme mobile centralisée conçue pour la gestion et l'animation d'un club e-sport universitaire. L'application vise à connecter les joueurs, à simplifier l'organisation des équipes et des événements, et à renforcer l'esprit de communauté en fournissant des outils de communication et de suivi en temps réel. Elle servira de hub central pour toutes les activités du club, des entraînements amicaux aux tournois officiels.

### 1.2 Objectifs du projet

- **Centralisation** : Unifier toutes les activités du club esport dans une seule plateforme
- **Automatisation** : Automatiser la gestion des tournois, brackets, et inscriptions
- **Communication** : Faciliter la communication entre membres, équipes et organisateurs
- **Suivi en temps réel** : Permettre le suivi des matchs, scores et résultats en temps réel
- **Accessibilité** : Offrir un accès multi-plateformes (Android, Web) pour une utilisation flexible

### 1.3 Public cible

- **Membres du club** : Joueurs souhaitant participer aux activités
- **Capitaines d'équipe** : Responsables de la gestion de leurs équipes
- **Administrateurs** : Organisateurs de tournois et gestionnaires du club
- **Communauté universitaire** : Étudiants intéressés par l'esport

### 1.4 Fonctionnalités principales

- Gestion des utilisateurs et authentification sécurisée
- Création et gestion d'équipes
- Organisation d'événements et tournois
- Génération automatique de brackets
- Suivi des matchs et scores
- Système de notifications complet
- Intégration Twitch pour les profils de streamers
- Assistant IA contextuel
- Géolocalisation pour les événements en présentiel
- Interface multi-plateformes (Android native + Web Angular)

---

## 2. Analyse et conception

### 2.1 Analyse des besoins

#### 2.1.1 Besoins fonctionnels

**Gestion des utilisateurs :**
- Inscription et authentification sécurisée
- Profils utilisateurs avec gamertag, préférences, localisation
- Système de rôles (Member, Captain, Admin)
- Liaison avec compte Twitch

**Gestion des équipes :**
- Création d'équipes par jeu
- Invitation et demande d'adhésion
- Gestion des membres (ajout, retrait, transfert de capitaine)
- Restrictions : un utilisateur ne peut être membre que d'une équipe active par jeu

**Gestion des événements :**
- Création d'événements (en ligne ou en présentiel)
- Dates d'inscription et de déroulement
- Formats multiples (1v1, 2v2, 3v3, 4v4, 5v5, BATTLE_ROYALE)
- Génération automatique de brackets
- Géolocalisation pour événements physiques

**Gestion des matchs :**
- Création automatique lors de la génération du bracket
- Suivi des statuts (upcoming, in_progress, finished, cancelled)
- Enregistrement et validation des scores
- Avancement automatique des vainqueurs
- Terminaison automatique de l'événement

**Système de notifications :**
- Plus de 40 types de notifications
- Notifications programmées
- Priorités (haute, moyenne, basse)
- Nettoyage automatique

#### 2.1.2 Besoins non fonctionnels

- **Sécurité** : Authentification JWT, hachage des mots de passe, validation des entrées
- **Performance** : Cache Redis, index MongoDB, requêtes optimisées
- **Scalabilité** : Architecture modulaire, séparation des responsabilités
- **Maintenabilité** : Code structuré, documentation complète
- **Disponibilité** : Gestion d'erreurs robuste, validation des données

### 2.2 Modélisation des cas d'utilisation

#### 2.2.1 Acteurs

- **Member** : Membre du club, peut créer une équipe, rejoindre des équipes, participer aux événements
- **Captain** : Capitaine d'équipe, peut gérer son équipe, inscrire l'équipe aux événements
- **Admin** : Administrateur, peut gérer tous les aspects de la plateforme

#### 2.2.2 Cas d'utilisation principaux

**UC1 : S'inscrire et se connecter**
- Acteur : Member
- Préconditions : Aucune
- Scénario : L'utilisateur s'inscrit avec email, mot de passe, nom, prénom, gamertag. Il reçoit un token JWT pour l'authentification.

**UC2 : Créer une équipe**
- Acteur : Member
- Préconditions : Utilisateur connecté, pas déjà capitaine d'une équipe active pour ce jeu
- Scénario : L'utilisateur crée une équipe, devient automatiquement captain.

**UC3 : Inscrire une équipe à un événement**
- Acteur : Captain
- Préconditions : Équipe créée, événement en statut "open", dates d'inscription respectées
- Scénario : Le captain sélectionne les membres participants et inscrit l'équipe.

**UC4 : Générer un bracket**
- Acteur : Admin
- Préconditions : Événement créé, inscriptions acceptées, pas de matchs joués
- Scénario : L'admin génère le bracket, les matchs sont créés automatiquement.

**UC5 : Gérer un match**
- Acteur : Admin
- Préconditions : Match créé dans le bracket
- Scénario : L'admin met à jour le statut, enregistre le score, valide le résultat. Le vainqueur avance automatiquement.

**UC6 : Rejoindre une équipe**
- Acteur : Member
- Préconditions : Utilisateur connecté, pas déjà membre d'une équipe active pour ce jeu
- Scénario : L'utilisateur crée une demande d'adhésion. Le capitaine accepte ou rejette la demande. Si acceptée, l'utilisateur devient membre de l'équipe.
- Cas d'échec : Si l'utilisateur est déjà membre d'une équipe active pour ce jeu, la demande est rejetée avec le message "Vous êtes déjà membre d'une équipe active pour ce jeu".

**UC7 : Tentative de créer une deuxième équipe (échec)**
- Acteur : Member/Captain
- Préconditions : Utilisateur déjà capitaine d'une équipe active pour un jeu
- Scénario : L'utilisateur tente de créer une nouvelle équipe pour le même jeu. La création est refusée avec le message "Vous êtes déjà capitaine d'une équipe active pour ce jeu".
- Postconditions : Aucune nouvelle équipe n'est créée.

**UC8 : Tentative de rejoindre une deuxième équipe du même jeu (échec)**
- Acteur : Member
- Préconditions : Utilisateur déjà membre d'une équipe active pour un jeu
- Scénario : L'utilisateur tente de créer une demande d'adhésion pour une autre équipe du même jeu. La demande est refusée avec le message "Vous êtes déjà membre d'une équipe active pour ce jeu".
- Postconditions : Aucune nouvelle demande n'est créée.

**UC9 : Quitter une équipe**
- Acteur : Member (non-capitaine)
- Préconditions : Utilisateur membre d'une équipe, pas de participation à un événement en cours
- Scénario : L'utilisateur quitte l'équipe. Il est retiré de la liste des membres.
- Cas d'échec : Si l'utilisateur participe à un événement en cours, la tentative de quitter est refusée avec le message "Impossible de quitter l'équipe si vous participez à un événement en cours".

**UC10 : Transférer le rôle de capitaine**
- Acteur : Captain
- Préconditions : Équipe avec au moins un autre membre
- Scénario : Le capitaine transfère son rôle à un autre membre de l'équipe. Le nouveau membre devient capitaine et l'ancien capitaine devient membre simple.
- Postconditions : Le rôle du nouveau capitaine est mis à jour dans son profil utilisateur.

**UC11 : Retirer un membre d'une équipe**
- Acteur : Captain
- Préconditions : Membre à retirer n'est pas le capitaine, pas de participation à un événement en cours
- Scénario : Le capitaine retire un membre de l'équipe. Le membre est retiré de la liste des membres.
- Cas d'échec : Si le membre participe à un événement en cours, la tentative de retrait est refusée avec le message "Impossible de retirer un membre participant à un événement en cours".

**UC12 : Modifier un événement**
- Acteur : Admin
- Préconditions : Événement créé, statut différent de "in_progress"
- Scénario : L'admin modifie les détails de l'événement (nom, dates, format, etc.). Les modifications sont sauvegardées.
- Cas d'échec : Si l'événement est en statut "in_progress", la modification est refusée avec le message "Impossible de modifier un événement en cours".

**UC13 : Annuler une inscription à un événement**
- Acteur : Captain
- Préconditions : Équipe inscrite à un événement, événement pas en statut "in_progress"
- Scénario : Le capitaine annule l'inscription de son équipe à l'événement. L'inscription est supprimée.
- Cas d'échec : Si l'événement est en statut "in_progress", l'annulation est refusée avec le message "Impossible d'annuler une inscription pour un événement en cours".

**UC14 : Inscrire une équipe à un événement (validation du format)**
- Acteur : Captain
- Préconditions : Équipe créée, événement en statut "open", dates d'inscription respectées
- Scénario : Le capitaine sélectionne les membres participants selon le format (ex: 5v5 = 5 membres). L'inscription est créée.
- Cas d'échec : Si le nombre de membres sélectionnés ne correspond pas au format requis, l'inscription est refusée avec le message "Le format X nécessite exactement Y joueurs".

**UC15 : Générer un bracket (avec prévention de régénération)**
- Acteur : Admin
- Préconditions : Événement créé, inscriptions acceptées
- Scénario : L'admin génère le bracket. Les matchs sont créés automatiquement selon le nombre d'équipes inscrites.
- Cas d'échec : Si des matchs ont déjà été joués, la régénération est refusée avec le message "Impossible de régénérer le bracket si des matchs ont été joués".

### 2.3 Règles de gestion

#### 2.3.1 Règles d'équipe
- Un utilisateur ne peut être capitaine que d'une seule équipe active par jeu
- Un utilisateur ne peut être membre que d'une seule équipe active par jeu
- Les admins ne peuvent pas rejoindre d'équipes
- Impossible de retirer un membre participant à un événement en cours
- Impossible de quitter l'équipe si participation à un événement en cours

#### 2.3.2 Règles d'événement
- Inscription uniquement par le capitaine
- Nombre exact de membres selon le format (ex: 5v5 = 5 membres)
- Un membre ne peut participer qu'à une seule équipe par événement
- Dates d'inscription doivent être respectées
- Impossible d'annuler une inscription si l'événement est en cours
- Impossible de modifier un événement en cours
- Impossible de régénérer le bracket si des matchs ont été joués

#### 2.3.3 Règles de match
- Seuls les admins peuvent modifier le statut, le score et valider
- Modification du score uniquement si statut "in_progress"
- Validation par un admin met automatiquement le match en "finished"
- Le gagnant est déterminé automatiquement selon le score
- Avancement automatique des vainqueurs dans le bracket
- Terminaison automatique de l'événement quand un vainqueur final est déterminé

---

## 3. Architecture

### 3.1 Architecture générale

Le projet suit une architecture **multi-tier** avec séparation claire des responsabilités :

```
┌─────────────────────────────────────────────────────────────┐
│                    Clients (Frontend)                        │
├──────────────────────┬──────────────────────────────────────┤
│  Android (Kotlin)   │    Angular (TypeScript)              │
│  Jetpack Compose    │    Feature-first                      │
└──────────┬──────────┴──────────────┬─────────────────────────┘
           │                         │
           │      HTTP/REST API       │
           │      (JWT Auth)         │
           │                         │
           └──────────┬──────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                  Backend (Node.js/Express)                   │
├──────────────────────────────────────────────────────────────┤
│  Routes → Controllers → Services → Models → Database       │
└─────────────────────┬───────────────────────────────────────┘
                      │
         ┌─────────────┼─────────────┐
         │             │             │
┌───────▼──────┐ ┌───▼──────┐ ┌───▼────────┐
│   MongoDB    │ │  Redis   │ │  External  │
│  (Données)   │ │  (Cache) │ │   APIs     │
│              │ │          │ │ (Twitch,    │
│              │ │          │ │  OpenAI)   │
└──────────────┘ └──────────┘ └────────────┘
```

### 3.2 Architecture Backend

#### 3.2.1 Structure des dossiers

```
l4m-esports-server/
├── app.js                    # Point d'entrée de l'application
├── config/                   # Configuration
│   ├── database.js          # Connexion MongoDB
│   └── env.js                # Variables d'environnement
├── models/                   # Modèles Mongoose
│   ├── user.model.js
│   ├── game.model.js
│   ├── team.model.js
│   ├── event.model.js
│   ├── match.model.js
│   ├── notification.model.js
│   ├── eventRegistration.model.js
│   └── teamRequest.model.js
├── routes/                   # Routes Express
│   ├── auth.routes.js
│   ├── user.routes.js
│   ├── game.routes.js
│   ├── team.routes.js
│   ├── event.routes.js
│   ├── match.routes.js
│   ├── notification.routes.js
│   ├── twitch.routes.js
│   └── ai.routes.js
├── controllers/              # Contrôleurs (logique de requête)
│   ├── auth.controller.js
│   ├── user.controller.js
│   └── ...
├── services/                 # Services (logique métier)
│   ├── auth.service.js
│   ├── user.service.js
│   ├── event.service.js
│   ├── match.service.js
│   ├── notification.service.js
│   ├── ai.service.js
│   └── twitch.service.js
├── middlewares/              # Middlewares Express
│   ├── auth.middleware.js   # Authentification JWT
│   ├── error.middleware.js  # Gestion des erreurs
│   └── validation.middleware.js
├── utils/                    # Utilitaires
│   └── errors.js            # Classes d'erreurs personnalisées
└── scripts/                  # Scripts d'initialisation
    ├── initDefaultAdmin.js
    ├── initDefaultGames.js
    ├── initDefaultUsers.js
    └── initDefaultEvents.js
```

#### 3.2.2 Flux de traitement des requêtes

```
Requête HTTP
    ↓
Middleware CORS / JSON Parser
    ↓
Route Express
    ↓
Middleware d'authentification (si nécessaire)
    ↓
Controller (validation des paramètres)
    ↓
Service (logique métier)
    ↓
Model (accès base de données)
    ↓
MongoDB / Redis
    ↓
Réponse JSON
```

#### 3.2.3 Technologies Backend

- **Node.js** : Runtime JavaScript côté serveur
- **Express.js** : Framework web minimaliste et flexible
- **MongoDB** : Base de données NoSQL orientée documents
- **Mongoose** : ODM (Object Document Mapper) pour MongoDB
- **Redis** : Cache en mémoire pour améliorer les performances
- **JWT (JSON Web Tokens)** : Authentification stateless
- **bcryptjs** : Hachage des mots de passe
- **Axios** : Client HTTP pour les appels API externes (Twitch, OpenAI)

### 3.3 Architecture Android

#### 3.3.1 Structure des dossiers

```
l4m-esports-mobile/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/l4m_esports_mobile/
│   │   │   ├── data/
│   │   │   │   ├── local/              # DataStore, préférences
│   │   │   │   ├── model/           # Modèles de données
│   │   │   │   │   ├── request/    # Requêtes API
│   │   │   │   │   └── response/   # Réponses API
│   │   │   │   ├── remote/         # Services Retrofit
│   │   │   │   └── repository/     # Repositories
│   │   │   ├── ui/
│   │   │   │   ├── screens/        # Écrans Compose
│   │   │   │   │   ├── auth/
│   │   │   │   │   │   ├── games/
│   │   │   │   │   │   ├── teams/
│   │   │   │   │   │   ├── events/
│   │   │   │   │   │   ├── matches/
│   │   │   │   │   │   ├── notifications/
│   │   │   │   │   │   └── profile/
│   │   │   │   │   ├── components/  # Composants réutilisables
│   │   │   │   │   └── viewmodel/  # ViewModels
│   │   │   ├── navigation/         # Navigation Compose
│   │   │   ├── di/                 # Injection de dépendances (Hilt)
│   │   │   ├── util/               # Utilitaires
│   │   │   └── MainActivity.kt
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
└── build.gradle.kts
```

#### 3.3.2 Architecture Android (MVVM)

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Screens    │  │  Components  │  │  Navigation  │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │
│         │                  │                   │          │
│         └──────────────────┼──────────────────┘          │
│                            │                             │
└────────────────────────────┼─────────────────────────────┘
                             │
                    ┌────────▼────────┐
                    │   ViewModels    │
                    │  (StateFlow)   │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │   Repositories  │
                    └────────┬────────┘
                             │
         ┌─────────────────────┼─────────────────────┐
         │                     │                     │
┌───────▼────────┐  ┌─────────▼─────────┐  ┌───────▼────────┐
│  API Services  │  │  Local Storage   │  │   Utilities    │
│   (Retrofit)   │  │   (DataStore)   │  │  (MapUtils,     │
│                │  │                  │  │   Location)    │
└────────────────┘  └──────────────────┘  └────────────────┘
```

#### 3.3.3 Technologies Android

- **Kotlin** : Langage de programmation principal
- **Jetpack Compose** : Framework UI déclaratif moderne
- **Hilt** : Injection de dépendances
- **Retrofit** : Client HTTP type-safe
- **Gson** : Sérialisation/désérialisation JSON
- **Coroutines** : Programmation asynchrone
- **ViewModel** : Gestion de l'état UI
- **Navigation Compose** : Navigation entre écrans
- **DataStore** : Stockage des préférences utilisateur
- **Coil** : Chargement d'images
- **Google Play Services Location** : Géolocalisation

### 3.4 Architecture Angular

#### 3.4.1 Structure des dossiers

```
l4m-esports-angular/
├── src/
│   ├── app/
│   │   ├── core/                    # Services transverses
│   │   │   ├── guards/              # Guards d'authentification
│   │   │   ├── interceptors/        # Intercepteurs HTTP
│   │   │   ├── services/            # Services globaux
│   │   │   └── models.ts             # Modèles partagés
│   │   ├── shared/                  # Composants réutilisables
│   │   ├── api/                     # Client API centralisé
│   │   ├── features/                # Features (lazy-loaded)
│   │   │   ├── auth/
│   │   │   ├── games/
│   │   │   ├── teams/
│   │   │   ├── events/
│   │   │   ├── matches/
│   │   │   ├── notifications/
│   │   │   └── profile/
│   │   ├── app.config.ts             # Configuration principale
│   │   └── app.routes.ts             # Routes
│   └── assets/
└── package.json
```

#### 3.4.2 Principes architecturaux Angular

- **Feature-first** : Organisation par fonctionnalité métier
- **Standalone components** : Composants autonomes (pas de NgModules)
- **Signals** : Gestion réactive de l'état
- **Lazy loading** : Chargement à la demande des features
- **Services + Signals** : État local par feature
- **API centralisée** : Client REST/WS typé dans `api/`

---

## 4. Schéma de la base de données

### 4.1 Modèle conceptuel de données

Le système utilise MongoDB, une base de données NoSQL orientée documents. Les relations entre les entités sont gérées via des références (ObjectId) et des tableaux de références.

### 4.2 Diagramme entité-relation

```
┌─────────────┐
│    User     │
├─────────────┤
│ _id         │
│ email       │◄──┐
│ password    │   │
│ firstName   │   │
│ lastName    │   │
│ gamertag    │   │
│ role        │   │
│ twitchUsername│ │
│ preferences │   │
│ location    │   │
│ avatar      │   │
└─────────────┘   │
      │           │
      │ 1         │ N
      │           │
      │ N         │
┌─────▼───────────┴──┐
│       Team         │
├────────────────────┤
│ _id                │
│ name               │
│ logo               │
│ game (ref)         │───┐
│ captain (ref User) │   │
│ members (ref User[])│   │
│ status             │   │
│ maxMembers         │   │
└────────────────────┘   │
      │                  │
      │ 1                │ N
      │                  │
      │ N                │
┌─────▼──────────────────┴──┐
│   EventRegistration        │
├────────────────────────────┤
│ _id                        │
│ event (ref Event)          │───┐
│ team (ref Team)           │   │
│ registeredBy (ref User)    │   │
│ participatingMembers (ref)│   │
│ status                     │   │
└────────────────────────────┘   │
      │                          │
      │                          │
┌─────▼───────────────────────────┴──┐
│          Event                     │
├────────────────────────────────────┤
│ _id                                │
│ name                               │
│ game (ref Game)                    │───┐
│ startDate                          │   │
│ endDate                            │   │
│ registrationStartDate              │   │
│ registrationEndDate                │   │
│ format                             │   │
│ rules                               │   │
│ description                        │   │
│ status                             │   │
│ location {type, address, coords}   │   │
│ maxTeams                           │   │
│ createdBy (ref User)               │   │
│ bracket {rounds[]}                 │   │
└────────────────────────────────────┘   │
      │                                  │
      │ 1                                │ N
      │                                  │
      │ N                                │
┌─────▼──────────────────────────────────┴──┐
│              Match                         │
├────────────────────────────────────────────┤
│ _id                                        │
│ event (ref Event)                          │
│ teams (ref Team[])                        │
│ scheduledTime                              │
│ actualStartTime                            │
│ actualEndTime                              │
│ status                                     │
│ score {team1, team2}                       │
│ bracketPosition {round, matchNumber}        │
│ winner (ref Team)                           │
│ validatedBy [{user, validatedAt}]         │
│ notes                                      │
└────────────────────────────────────────────┘

┌─────────────┐
│    Game     │
├─────────────┤
│ _id         │
│ name        │
│ description │
│ logo        │
│ rules       │
│ formats[]   │
│ isActive    │
│ createdBy   │
└─────────────┘

┌─────────────┐
│ Notification│
├─────────────┤
│ _id         │
│ user (ref)  │
│ type        │
│ title       │
│ message     │
│ read        │
│ readAt      │
│ relatedEntity│
│ scheduledFor│
│ sent        │
└─────────────┘

┌─────────────┐
│TeamRequest  │
├─────────────┤
│ _id         │
│ team (ref)  │
│ user (ref)  │
│ status      │
└─────────────┘
```

### 4.3 Collections principales

#### 4.3.1 Collection User

```javascript
{
  _id: ObjectId,
  email: String (unique, required),
  password: String (hashed, required),
  firstName: String (required),
  lastName: String (required),
  gamertag: String (unique, required),
  role: String (enum: ['member', 'captain', 'admin'], default: 'member'),
  twitchUsername: String (optional, unique),
  preferences: {
    favoriteGames: [ObjectId (ref: 'Game')],
    notificationSettings: {
      matchReminders: Boolean,
      eventNearby: Boolean
    }
  },
  location: {
    latitude: Number,
    longitude: Number
  },
  avatar: String,
  createdAt: Date,
  updatedAt: Date
}
```

**Index :**
- `email` : unique
- `gamertag` : unique
- `twitchUsername` : sparse unique

#### 4.3.2 Collection Game

```javascript
{
  _id: ObjectId,
  name: String (unique, required),
  description: String,
  logo: String (URL),
  rules: String,
  formats: [String] (enum: ['1v1', '2v2', '3v3', '4v4', '5v5', 'BATTLE_ROYALE']),
  isActive: Boolean (default: true),
  createdBy: ObjectId (ref: 'User'),
  createdAt: Date,
  updatedAt: Date
}
```

**Index :**
- `name` : unique
- `isActive` : 1

#### 4.3.3 Collection Team

```javascript
{
  _id: ObjectId,
  name: String (required),
  logo: String (URL),
  game: ObjectId (ref: 'Game', required),
  description: String,
  captain: ObjectId (ref: 'User', required),
  members: [ObjectId] (ref: 'User'),
  status: String (enum: ['active', 'inactive'], default: 'active'),
  maxMembers: Number (default: 5),
  createdAt: Date,
  updatedAt: Date
}
```

**Index :**
- `game` : 1
- `status` : 1
- `captain` : 1

#### 4.3.4 Collection Event

```javascript
{
  _id: ObjectId,
  name: String (required),
  game: ObjectId (ref: 'Game', required),
  startDate: Date (required),
  endDate: Date (required),
  registrationStartDate: Date (required),
  registrationEndDate: Date (required),
  format: String (enum: ['1v1', '2v2', '3v3', '4v4', '5v5', 'BATTLE_ROYALE']),
  rules: String,
  description: String,
  status: String (enum: ['draft', 'open', 'registration_closed', 'in_progress', 'completed', 'cancelled']),
  location: {
    type: String (enum: ['online', 'physical']),
    address: String,
    coordinates: {
      type: String (default: 'Point'),
      coordinates: [Number] // [longitude, latitude]
    }
  },
  maxTeams: Number (default: 16),
  createdBy: ObjectId (ref: 'User', required),
  bracketGenerated: Boolean (default: false),
  bracket: {
    rounds: [{
      roundNumber: Number,
      matches: [{
        team1: ObjectId (ref: 'Team'),
        team2: ObjectId (ref: 'Team'),
        winner: ObjectId (ref: 'Team')
      }]
    }]
  },
  createdAt: Date,
  updatedAt: Date
}
```

**Index :**
- `location.coordinates` : 2dsphere (géospatial)
- `status` : 1
- `game` : 1
- `startDate` : 1

#### 4.3.5 Collection EventRegistration

```javascript
{
  _id: ObjectId,
  event: ObjectId (ref: 'Event', required),
  team: ObjectId (ref: 'Team', required),
  registeredBy: ObjectId (ref: 'User', required),
  participatingMembers: [ObjectId] (ref: 'User', required),
  status: String (enum: ['PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED'], default: 'PENDING'),
  registeredAt: Date (default: Date.now),
  createdAt: Date,
  updatedAt: Date
}
```

**Index :**
- `event` + `team` : unique compound
- `event` : 1
- `team` : 1
- `status` : 1

#### 4.3.6 Collection Match

```javascript
{
  _id: ObjectId,
  event: ObjectId (ref: 'Event', required),
  teams: [ObjectId] (ref: 'Team', required),
  scheduledTime: Date (required),
  actualStartTime: Date,
  actualEndTime: Date,
  status: String (enum: ['upcoming', 'in_progress', 'finished', 'pending_validation', 'cancelled']),
  score: {
    team1: Number (default: 0),
    team2: Number (default: 0)
  },
  bracketPosition: {
    round: Number (required),
    matchNumber: Number (required),
    bracketSide: String (enum: ['upper', 'lower', 'single'], default: 'single')
  },
  winner: ObjectId (ref: 'Team'),
  validatedBy: [{
    user: ObjectId (ref: 'User'),
    validatedAt: Date
  }],
  notes: String,
  createdAt: Date,
  updatedAt: Date
}
```

**Index :**
- `event` : 1
- `status` : 1
- `scheduledTime` : 1
- `bracketPosition.round` + `bracketPosition.matchNumber` : compound
- `teams` : 1

#### 4.3.7 Collection Notification

```javascript
{
  _id: ObjectId,
  user: ObjectId (ref: 'User', required),
  type: String (enum: [40+ types], required),
  title: String (required),
  message: String (required),
  read: Boolean (default: false),
  readAt: Date,
  relatedEntity: {
    entityType: String (enum: ['match', 'event', 'team', 'team_request']),
    entityId: ObjectId
  },
  scheduledFor: Date,
  sent: Boolean (default: false),
  sentAt: Date,
  createdAt: Date,
  updatedAt: Date
}
```

**Index :**
- `user` + `read` : compound
- `user` + `createdAt` : compound (desc)
- `scheduledFor` + `sent` : compound
- `type` : 1
- `read` + `createdAt` : compound (pour nettoyage)

#### 4.3.8 Collection TeamRequest

```javascript
{
  _id: ObjectId,
  team: ObjectId (ref: 'Team', required),
  user: ObjectId (ref: 'User', required),
  status: String (enum: ['pending', 'accepted', 'rejected', 'cancelled'], default: 'pending'),
  createdAt: Date,
  updatedAt: Date
}
```

**Index :**
- `team` + `user` + `status` : compound
- `team` : 1
- `user` : 1
- `status` : 1

### 4.4 Relations entre collections

- **User ↔ Team** : Relation 1-N (un utilisateur peut être membre de plusieurs équipes, mais une seule active par jeu)
- **User ↔ Team (captain)** : Relation 1-N (un utilisateur peut être capitaine de plusieurs équipes, mais une seule active par jeu)
- **Game ↔ Team** : Relation 1-N (un jeu peut avoir plusieurs équipes)
- **Game ↔ Event** : Relation 1-N (un jeu peut avoir plusieurs événements)
- **Event ↔ EventRegistration** : Relation 1-N (un événement peut avoir plusieurs inscriptions)
- **Team ↔ EventRegistration** : Relation 1-N (une équipe peut s'inscrire à plusieurs événements)
- **Event ↔ Match** : Relation 1-N (un événement peut avoir plusieurs matchs)
- **Team ↔ Match** : Relation N-M (une équipe peut participer à plusieurs matchs)
- **User ↔ Notification** : Relation 1-N (un utilisateur peut avoir plusieurs notifications)

---

## 5. Mise en œuvre

### 5.1 Technologies utilisées

#### 5.1.1 Backend
- **Node.js** v18+
- **Express.js** v4.18.2
- **MongoDB** avec Mongoose v8.0.3
- **Redis** v4.6.12
- **JWT** (jsonwebtoken) v9.0.2
- **bcryptjs** v2.4.3
- **Axios** v1.13.2
- **Socket.io** v4.7.4 : Communication en temps réel

#### 5.1.2 Android
- **Kotlin** 1.9+
- **Jetpack Compose** (Material 3)
- **Hilt** (Dependency Injection)
- **Retrofit** + **Gson**
- **Coroutines**
- **Navigation Compose**
- **DataStore**
- **Coil** (Image loading)
- **Google Play Services Location**

#### 5.1.3 Web (Angular)
- **Angular** 17+
- **TypeScript**
- **RxJS**
- **Standalone Components**
- **Signals**
- **Socket.io Client** : Synchronisation en temps réel

### 5.2 Fonctionnalités implémentées

#### 5.2.1 Authentification et gestion des utilisateurs

**Backend :**
- Inscription avec validation (email unique, gamertag unique)
- Connexion avec génération de token JWT
- Déconnexion avec révocation du token
- Gestion du profil utilisateur (modification, suppression)
- Gestion des rôles (Member, Captain, Admin)
- Liaison avec compte Twitch

**Android :**
- Écrans de connexion et d'inscription
- Stockage sécurisé du token JWT (DataStore)
- Gestion du profil utilisateur
- Déconnexion et suppression de compte
- Intégration Twitch (test et aperçu)

**Angular :**
- Services d'authentification
- Guards de route
- Intercepteurs HTTP pour l'ajout automatique du token
- Gestion des utilisateurs (Admin uniquement)
- Synchronisation en temps réel via Socket.io

#### 5.2.2 Gestion des jeux

**Backend :**
- CRUD complet des jeux (Admin uniquement)
- Support de multiples formats (1v1, 2v2, 3v3, 4v4, 5v5, BATTLE_ROYALE)
- Statut actif/inactif
- Règles et descriptions personnalisables

**Android :**
- Liste des jeux avec filtres
- Détails d'un jeu
- Création/modification (Admin uniquement)

#### 5.2.3 Gestion des équipes

**Backend :**
- Création d'équipes (utilisateur devient automatiquement captain)
- Invitation de membres
- Demandes d'adhésion avec acceptation/rejet
- Transfert de capitaine
- Retrait de membres (avec restrictions si événement en cours)
- Quitter une équipe (avec restrictions)
- Suppression d'équipe (Admin/Captain)
- Filtrage des jeux disponibles lors de la création (exclut les jeux où l'utilisateur est déjà capitaine ou membre)

**Android :**
- Liste des équipes avec filtres (jeu, statut)
- Détails d'une équipe
- Création/modification d'équipe (avec filtrage des jeux disponibles)
- Gestion des membres
- Gestion des demandes d'adhésion
- Transfert de capitaine
- Synchronisation en temps réel via Socket.io

#### 5.2.4 Gestion des événements

**Backend :**
- CRUD complet des événements (Admin uniquement)
- Support événements en ligne et en présentiel
- Géolocalisation avec coordonnées GPS
- Génération automatique de brackets
- Prévention de régénération si matchs joués
- Avancement automatique des vainqueurs
- Terminaison automatique de l'événement quand un vainqueur final est déterminé
- Recherche d'événements à proximité (géolocalisation)
- Mise à jour automatique du statut basé sur les dates et la complétion des matchs
- Synchronisation en temps réel via Socket.io

**Android :**
- Liste des événements avec filtres (jeu, statut, à venir)
- Filtrage des événements en présentiel
- Détails d'un événement avec bracket
- Affichage du vainqueur final
- Intégration Google Maps pour les événements en présentiel
- Création/modification d'événement (Admin uniquement)
- Synchronisation en temps réel via Socket.io

#### 5.2.5 Inscriptions aux événements

**Backend :**
- Inscription d'une équipe à un événement (Captain uniquement)
- Validation du nombre de membres selon le format
- Vérification des dates d'inscription
- Validation par les administrateurs
- Modification et annulation d'inscription (avec restrictions)

**Android :**
- Liste des inscriptions
- Détails d'une inscription
- Inscription à un événement
- Modification/annulation d'inscription

#### 5.2.6 Gestion des matchs

**Backend :**
- Création automatique lors de la génération du bracket
- Mise à jour du statut (Admin uniquement)
- Mise à jour du score (Admin uniquement, uniquement si statut "in_progress")
- Validation des résultats (Admin uniquement)
- Détermination automatique du vainqueur
- Avancement automatique dans le bracket
- Rechargement automatique du bracket après validation
- Transition automatique : in_progress → pending_validation → finished
- Synchronisation en temps réel via Socket.io

**Android :**
- Liste des matchs avec filtres (événement, équipe, statut)
- Détails d'un match
- Affichage du vainqueur pour les matchs terminés
- Mise à jour du statut/score/validation (Admin uniquement)
- Affichage des validateurs
- Synchronisation en temps réel via Socket.io

#### 5.2.7 Système de notifications

**Backend :**
- Plus de 40 types de notifications :
  - **Matchs** : `match_created`, `match_starting_soon`, `match_status_changed`, `match_score_updated`, `match_finished`, `match_won`, `match_lost`, `match_cancelled`, `match_rescheduled`
  - **Équipes** : `team_request`, `team_request_accepted`, `team_request_rejected`, `team_invitation`, `team_invitation_accepted`, `team_invitation_rejected`, `team_member_joined`, `team_member_removed`, `team_member_left`, `team_captain_transferred`, `team_updated`
  - **Événements** : `event_created`, `event_started`, `event_completed`, `event_cancelled`, `event_updated`, `event_registration_open`, `event_registration_closed`, `event_registration_created`, `event_registration_accepted`, `event_registration_rejected`, `event_registration_cancelled`, `next_round_created`, `bracket_updated`
- Notifications programmées (ex: 15 minutes avant un match)
- Priorités (haute, moyenne, basse)
- Nettoyage automatique des anciennes notifications lues
- Compteur de notifications non lues

**Android :**
- Liste des notifications avec filtres
- Marquer comme lue / toutes comme lues
- Suppression de notifications
- Badge avec compteur de non lues
- Affichage des notifications par priorité

#### 5.2.8 Intégration Twitch

**Backend :**
- Récupération des informations utilisateur Twitch (display name, description, image de profil, vues, followers, statut live)
- Liaison du compte Twitch au profil utilisateur

**Android :**
- Champ de saisie du nom d'utilisateur Twitch
- Bouton de test pour vérifier le compte
- Aperçu des informations Twitch (avatar, description, statistiques)
- Affichage du statut live

#### 5.2.9 Assistant IA

**Backend :**
- Intégration OpenAI (GPT)
- Contexte personnalisé selon la page et le rôle de l'utilisateur
- Réponses intelligentes aux questions des utilisateurs

**Android :**
- Widget flottant d'assistant IA
- Interface de chat
- Contexte automatique (page actuelle, rôle utilisateur)
- Gestion des états (chargement, erreur, succès)

#### 5.2.10 Géolocalisation

**Backend :**
- Support des événements en présentiel avec coordonnées GPS
- Index géospatial MongoDB pour recherche efficace
- Endpoint `/api/events/nearby` pour événements à proximité

**Android :**
- Demande de permissions de localisation
- Récupération de la position GPS de l'utilisateur
- Filtrage des événements en présentiel
- Intégration Google Maps pour afficher le lieu sur une carte
- Bouton "Voir sur Google Maps" dans les détails d'événement

### 5.3 Points techniques remarquables

#### 5.3.1 Gestion des erreurs
- Classes d'erreurs personnalisées (`NotFoundError`, `ValidationError`, etc.)
- Middleware de gestion d'erreurs centralisé
- Messages d'erreur clairs et informatifs

#### 5.3.2 Performance
- Utilisation de Redis pour le cache
- Index MongoDB pour les requêtes fréquentes
- Index géospatial pour la recherche d'événements à proximité
- Lazy loading dans Angular

#### 5.3.3 Sérialisation JSON
- Deserializers Gson personnalisés pour gérer les références polymorphes (string ID vs objet complet)
- Gestion des cas où les champs peuvent être soit un ID string, soit un objet complet

#### 5.3.4 Notifications
- Système de notifications programmées avec `scheduledFor`
- Nettoyage automatique des anciennes notifications
- Index optimisés pour les requêtes de notifications

#### 5.3.5 Bracket
- Génération automatique de brackets single elimination
- Prévention de régénération si matchs joués
- Avancement automatique des vainqueurs
- Terminaison automatique de l'événement

### 5.4 Scripts d'initialisation

Le projet inclut des scripts pour initialiser la base de données avec des données de test :

- **`initDefaultAdmin.js`** : Crée un administrateur par défaut
- **`initDefaultGames.js`** : Crée des jeux de test (League of Legends, Valorant, Counter-Strike, etc.)
- **`initDefaultUsers.js`** : Crée des utilisateurs, équipes et membres de test
- **`initDefaultEvents.js`** : Crée des événements de test avec brackets, incluant un événement en présentiel à Montréal pour tester la géolocalisation

### 5.5 Tests

**Backend :**
- Tests unitaires avec Jest
- Tests d'intégration pour les routes principales
- Configuration MongoDB Memory Server pour les tests
- Tests couvrant : authentification, jeux, équipes, demandes d'adhésion, utilisateurs

### 5.6 Déploiement

**Backend :**
- Configuration Docker avec `docker-compose.yml`
- Support MongoDB et Redis via Docker
- Variables d'environnement pour la configuration

**Android :**
- Configuration Gradle pour build debug/release
- Support Android 24+ (Android 7.0+)
- Target SDK 36 (Android 14)

**Angular :**
- Configuration pour développement local avec proxy
- Prêt pour déploiement (build production)

---

## 6. Présentation du résultat

### 6.1 Application Android

#### 6.1.1 Écran d'accueil et authentification

**Écran de connexion :**
- Formulaire avec champs email et mot de passe
- Bouton "Se connecter"
- Lien vers l'inscription
- Gestion des erreurs d'authentification

**Écran d'inscription :**
- Formulaire avec email, mot de passe, prénom, nom, gamertag
- Validation en temps réel
- Messages d'erreur clairs

#### 6.1.2 Navigation principale

**Barre de navigation inférieure :**
- Onglet "Jeux" : Liste des jeux disponibles
- Onglet "Équipes" : Liste des équipes
- Onglet "Notifications" : Centre de notifications avec badge de compteur
- Onglet "Profil" : Profil utilisateur

#### 6.1.3 Écran des jeux

**Liste des jeux :**
- Cartes pour chaque jeu avec logo, nom, description
- Filtres par statut (actif/inactif)
- Bouton "+" pour créer un jeu (Admin uniquement)

**Détails d'un jeu :**
- Informations complètes du jeu
- Liste des événements associés
- Filtre "En présentiel" / "Tous" pour les événements
- Bouton pour créer un événement (Admin uniquement)

#### 6.1.4 Écran des équipes

**Liste des équipes :**
- Cartes pour chaque équipe avec logo, nom, jeu, statut
- Filtres par jeu et statut
- Bouton "+" pour créer une équipe

**Détails d'une équipe :**
- Informations de l'équipe
- Liste des membres avec rôle (capitaine/membre)
- Actions selon le rôle (modifier, inviter, gérer les demandes)
- Bouton pour voir les demandes d'adhésion

#### 6.1.5 Écran des événements

**Liste des événements :**
- Cartes pour chaque événement avec nom, jeu, format, statut, dates
- Filtres par jeu, statut, à venir
- Filtre "En présentiel" pour les événements physiques

**Détails d'un événement :**
- Informations complètes de l'événement
- Bracket du tournoi avec matchs et vainqueurs
- Affichage du vainqueur final si l'événement est terminé
- Section "Lieu" avec adresse et bouton "Voir sur Google Maps" (si événement en présentiel)
- Boutons pour générer/régénérer le bracket (Admin uniquement, désactivé si matchs joués)
- Liste des inscriptions (Admin uniquement)

#### 6.1.6 Écran des matchs

**Liste des matchs :**
- Cartes pour chaque match avec équipes, statut, score, date
- Filtres par événement, équipe, statut

**Détails d'un match :**
- Informations complètes du match
- Affichage des équipes avec logos
- Score actuel
- Statut du match
- Affichage du vainqueur pour les matchs terminés
- Actions admin : Modifier le statut, Modifier le score (si in_progress), Valider le résultat
- Liste des validateurs

#### 6.1.7 Centre de notifications

**Liste des notifications :**
- Cartes pour chaque notification avec titre, message, date, priorité
- Badge de compteur de notifications non lues
- Filtres par type, statut (lue/non lue)
- Actions : Marquer comme lue, Marquer toutes comme lues, Supprimer

#### 6.1.8 Profil utilisateur

**Profil :**
- Informations de l'utilisateur (nom, email, gamertag, rôle)
- Bouton pour modifier le profil
- Bouton pour se déconnecter
- Bouton pour supprimer le compte

**Modification du profil :**
- Formulaire avec tous les champs modifiables
- Section Twitch avec champ de saisie, bouton "Tester", aperçu des informations
- Bouton "Enregistrer"
- Scrollbar pour navigation
- Bouton retour

#### 6.1.9 Assistant IA

**Widget flottant :**
- Bouton flottant en bas à gauche de l'écran
- Interface de chat qui s'ouvre au clic
- Champ de saisie pour poser une question
- Bouton "Poser la question"
- Affichage de la réponse de l'IA
- Gestion des états (chargement, erreur, succès)
- Bouton "Nouvelle question" pour réinitialiser

### 6.2 Application Web (Angular)

#### 6.2.1 Interface web

**Layout principal :**
- Barre de navigation supérieure
- Menu latéral (si implémenté)
- Zone de contenu principal
- Footer

**Pages principales :**
- Page de connexion/inscription
- Dashboard
- Liste des jeux
- Liste des équipes
- Liste des événements
- Liste des matchs
- Centre de notifications
- Profil utilisateur

### 6.3 API Backend

#### 6.3.1 Endpoints principaux

**Authentification :**
- `POST /api/auth/sign-up` : Inscription
- `POST /api/auth/sign-in` : Connexion
- `POST /api/auth/sign-out` : Déconnexion

**Utilisateurs :**
- `GET /api/users/me` : Profil utilisateur connecté
- `PUT /api/users/me` : Modifier son profil
- `DELETE /api/users/me` : Supprimer son compte

**Jeux :**
- `GET /api/games` : Liste des jeux
- `GET /api/games/:id` : Détails d'un jeu
- `POST /api/games` : Créer un jeu (Admin)
- `PUT /api/games/:id` : Modifier un jeu (Admin)
- `DELETE /api/games/:id` : Supprimer un jeu (Admin)

**Équipes :**
- `GET /api/teams` : Liste des équipes
- `GET /api/teams/:id` : Détails d'une équipe
- `POST /api/teams` : Créer une équipe
- `PUT /api/teams/:id` : Modifier une équipe
- `POST /api/teams/:id/invite` : Inviter un membre

**Événements :**
- `GET /api/events` : Liste des événements
- `GET /api/events/:id` : Détails d'un événement
- `GET /api/events/:id/bracket` : Bracket d'un événement
- `POST /api/events/:id/generate-bracket` : Générer le bracket (Admin)
- `GET /api/events/nearby` : Événements à proximité

**Matchs :**
- `GET /api/matches` : Liste des matchs
- `GET /api/matches/:id` : Détails d'un match
- `PUT /api/matches/:id/status` : Modifier le statut (Admin)
- `PUT /api/matches/:id/score` : Modifier le score (Admin)
- `POST /api/matches/:id/validate` : Valider le résultat (Admin)

**Notifications :**
- `GET /api/notifications` : Liste des notifications
- `GET /api/notifications/unread-count` : Compteur de non lues
- `PUT /api/notifications/:id/read` : Marquer comme lue
- `PUT /api/notifications/read-all` : Marquer toutes comme lues

**Twitch :**
- `GET /api/twitch/user/:username` : Informations utilisateur Twitch

**IA :**
- `POST /api/ai/assist` : Poser une question à l'IA

### 6.4 Fonctionnalités avancées démontrées

#### 6.4.1 Génération de bracket
- Création automatique des matchs selon le nombre d'équipes inscrites
- Organisation en rounds (quart de finale, demi-finale, finale)
- Affichage visuel du bracket dans l'interface

#### 6.4.2 Avancement automatique
- Détection automatique du vainqueur selon le score
- Avancement du vainqueur au round suivant
- Terminaison automatique de l'événement quand un vainqueur final est déterminé

#### 6.4.3 Notifications en temps réel
- Notifications automatiques pour tous les événements importants
- Badge de compteur mis à jour en temps réel
- Notifications programmées (ex: 15 minutes avant un match)

#### 6.4.4 Intégration Google Maps
- Affichage du lieu d'un événement en présentiel sur Google Maps
- Ouverture de Google Maps avec les coordonnées GPS
- Fallback vers navigateur web si Google Maps n'est pas installé

#### 6.4.5 Assistant IA contextuel
- Réponses intelligentes selon le contexte (page actuelle, rôle utilisateur)
- Interface de chat intuitive
- Gestion des erreurs et états de chargement

---

## 7. Conclusion et perspectives

### 7.1 Bilan du projet

**L4M Esports** est une plateforme complète et robuste de gestion de tournois esports qui répond aux besoins de tous les acteurs de l'écosystème esport. Avec une architecture moderne, des fonctionnalités avancées (notifications, IA, géolocalisation, Twitch), et un support multi-plateformes (Android, Web), la solution offre une expérience utilisateur exceptionnelle tout en automatisant les tâches complexes de gestion de tournois.

Le projet démontre une maîtrise des technologies modernes (Node.js, Kotlin, Angular, MongoDB, Redis) et des bonnes pratiques de développement (architecture en couches, séparation des responsabilités, sécurité, tests).

### 7.2 Points forts

- **Architecture solide** : Séparation claire des responsabilités, code modulaire et maintenable
- **Fonctionnalités complètes** : Couverture de tous les besoins identifiés
- **Sécurité** : Authentification JWT, validation des données, permissions granulaires
- **Performance** : Cache Redis, index MongoDB, requêtes optimisées
- **Expérience utilisateur** : Interface intuitive, notifications en temps réel, assistant IA
- **Multi-plateformes** : Support Android et Web

### 7.3 Difficultés rencontrées

- **Gestion des références polymorphes** : Résolu avec des deserializers Gson personnalisés
- **Synchronisation bracket/matchs** : Résolu avec rechargement automatique après validation
- **Permissions complexes** : Résolu avec système de rôles et vérifications dans les services
- **Géolocalisation** : Résolu avec intégration Google Maps et fallback navigateur
- **Synchronisation en temps réel** : Résolu avec Socket.io pour synchroniser tous les clients
- **Mise à jour automatique du statut des événements** : Résolu avec vérification des dates et des matchs terminés
- **Gestion des capitaines supprimés** : Résolu avec transfert automatique de capitaine lors de la suppression

### 7.4 Améliorations futures possibles

- **Double elimination brackets** : Support des brackets à double élimination
- **Streaming intégré** : Intégration directe des streams Twitch dans l'application
- **Statistiques avancées** : Tableaux de bord avec statistiques détaillées
- **Chat en direct** : Chat pour les matchs et événements
- **Mode offline** : Support du mode hors ligne avec synchronisation
- **Push notifications** : Notifications push natives sur mobile
- **Multi-langues** : Support de plusieurs langues
- **Paiements** : Intégration de paiements pour les inscriptions payantes
- **Application iOS** : Extension à la plateforme iOS

### 7.5 Apprentissages

- Maîtrise de l'architecture multi-tier
- Gestion de bases de données NoSQL (MongoDB)
- Développement mobile moderne (Jetpack Compose)
- Développement web moderne (Angular standalone)
- Intégration d'APIs externes (Twitch, OpenAI)
- Gestion de la géolocalisation
- Système de notifications complexe
- Génération automatique de brackets

---

**Date de création** : 2025  
**Version** : 1.0.0  
**Auteurs** : Théodore Grignard, Xavier Dostie, Sébastien Drezet, Tony Besse
