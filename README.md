# L4M Esports - Documentation Complète

## 📋 Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Architecture](#architecture)
3. [Rôles et Permissions](#rôles-et-permissions)
4. [Authentification](#authentification)
5. [Routes API](#routes-api)
6. [Use Cases par Rôle](#use-cases-par-rôle)
7. [Modèles de Données](#modèles-de-données)
8. [Workflows Principaux](#workflows-principaux)

---

## Vue d'ensemble

**L4M Esports** est une plateforme de gestion de tournois esports permettant aux utilisateurs de créer des équipes, participer à des événements, gérer des matchs et suivre leurs performances.

### Technologies utilisées

- **Backend**: Node.js + Express.js
- **Base de données**: MongoDB avec Mongoose
- **Cache**: Redis
- **Authentification**: JWT (JSON Web Tokens)
- **Client Mobile**: Android (Kotlin + Jetpack Compose)
- **Client Web**: À développer (React/Vue/Angular recommandé)

---

## Architecture

### Structure du projet

```
l4m-esports/
├── l4m-esports-server/     # Backend API
│   ├── models/             # Modèles Mongoose
│   ├── routes/             # Routes Express
│   ├── controllers/        # Contrôleurs
│   ├── services/           # Logique métier
│   ├── middlewares/        # Middlewares (auth, validation)
│   ├── utils/              # Utilitaires
│   └── scripts/            # Scripts d'initialisation
│
└── l4m-esports-mobile/     # Application Android
    └── app/src/main/java/...
```

### Flux de données

```
Client (Web/Mobile)
    ↓
API Routes (Express)
    ↓
Controllers
    ↓
Services (Logique métier)
    ↓
Models (Mongoose)
    ↓
MongoDB
```

### Plan d'architecture Angular (front web, sans code)

- Approche : feature-first avec composants standalone, services + signals pour l'état local, et une lib API dédiée pour centraliser REST/WS.
- Objectifs : découplage par domaine (auth, users, teams, events, matches, notifications), lazy-loading par feature, base PWA + temps réel + mode offline.
- Arborescence proposée (nouveau dossier `l4m-esports-web` au même niveau que le serveur) :
  ```
  l4m-esports-web/
  ├── src/
  │   ├── app/
  │   │   ├── core/                 # Auth guard, interceptors, layout global, services transverses (auth, session, toasts)
  │   │   ├── shared/               # UI réutilisable, design tokens
  │   │   ├── api/                  # Client typé REST/WS, mapping modèles serveur -> UI
  │   │   ├── features/
  │   │   │   ├── auth/
  │   │   │   ├── users/
  │   │   │   ├── teams/
  │   │   │   ├── events/
  │   │   │   ├── matches/
  │   │   │   └── notifications/    # Centre de notif, bridge WebSocket, prêt pour push PWA
  │   │   └── app.config.ts         # Routing principal, providers (standalone)
  │   └── assets/                   # Icons, logos, manifest, service worker
  └── package.json
  ```
- Données et state : services + signals par feature ; ajouter un store global (NgRx/SignalStore) uniquement pour les flux complexes (matches/events en live + offline).
- Temps réel : un service WebSocket central dans `core` qui diffuse vers les features pour éviter la dispersion des sockets.
- Offline/PWA : manifest + service worker, cache HTTP de base et persistence locale (IndexedDB/localforage) pour les entités critiques si besoin.
- Intégration API : la lib `api/` encapsule les routes listées ici (auth, users, teams, events, matches, notifications) et expose des DTO typés alignés sur le backend.

---

## Rôles et Permissions

### 1. **Member** (Membre)
- Rôle par défaut lors de l'inscription
- Peut créer une équipe (devient automatiquement captain)
- Peut rejoindre une équipe via une demande
- Peut consulter les événements, matchs, équipes
- **Limitations**: Une seule équipe active par jeu

### 2. **Captain** (Capitaine)
- Automatiquement promu lors de la création d'une équipe
- Tous les droits d'un Member
- Peut gérer son équipe (modifier, supprimer)
- Peut inviter des membres
- Peut accepter/rejeter les demandes d'adhésion
- Peut inscrire son équipe à des événements
- Peut modifier/annuler les inscriptions (si l'événement n'est pas en cours)
- Peut mettre à jour le statut et le score des matchs de son équipe
- Peut valider les résultats des matchs
- **Limitations**: 
  - Ne peut pas quitter l'équipe si elle participe à un événement en cours
  - Ne peut pas retirer un membre participant à un événement en cours

### 3. **Admin** (Administrateur)
- Tous les droits
- Peut gérer les jeux (CRUD complet)
- Peut gérer les événements (CRUD complet)
- Peut générer/régénérer les brackets de tournois
- Peut voir toutes les inscriptions aux événements
- Peut modifier/supprimer n'importe quelle équipe
- Peut modifier le rôle des utilisateurs
- Peut gérer tous les matchs (statut, score, validation)
- **Restrictions**: 
  - Ne peut pas modifier un événement en cours (`in_progress`)
  - Ne peut pas rejoindre d'équipes
  - Ne peut pas être invité à des équipes

---

## Règles de Validation et Limitations

### 📋 Règles générales

#### Équipes
1. **Un utilisateur ne peut être capitaine que d'une seule équipe active par jeu**
   - Si un utilisateur est déjà capitaine d'une équipe active pour un jeu, il ne peut pas créer une autre équipe pour ce même jeu
   - Il peut cependant être capitaine d'équipes pour différents jeux

2. **Un utilisateur ne peut être membre que d'une seule équipe active par jeu**
   - Un utilisateur ne peut pas rejoindre une équipe s'il est déjà membre d'une autre équipe active pour le même jeu
   - Cette règle s'applique aussi lors de l'invitation d'un utilisateur

3. **Les admins ne peuvent pas rejoindre d'équipes**
   - Les admins ne peuvent pas créer de demandes d'adhésion
   - Les admins ne peuvent pas être invités à des équipes

4. **Limite de membres par équipe**
   - Chaque équipe a un `maxMembers` (par défaut 5)
   - Impossible d'ajouter un membre si l'équipe a atteint sa limite

5. **Le capitaine est automatiquement membre**
   - Le capitaine est toujours inclus dans la liste des membres
   - Impossible d'inviter le capitaine à sa propre équipe

#### Événements et Inscriptions
1. **Inscription uniquement par le capitaine**
   - Seul le capitaine de l'équipe peut inscrire son équipe à un événement

2. **Nombre exact de membres selon le format**
   - Le format de l'événement détermine le nombre exact de joueurs requis :
     - `1v1` = 1 joueur
     - `2v2` = 2 joueurs
     - `3v3` = 3 joueurs
     - `4v4` = 4 joueurs
     - `5v5` = 5 joueurs
     - `BATTLE_ROYALE` = 100 joueurs
   - Le capitaine est toujours inclus automatiquement dans le compte
   - Il faut sélectionner exactement le bon nombre de membres supplémentaires

3. **Un membre ne peut participer qu'à une seule équipe par événement**
   - Si un membre participe déjà à un événement avec une équipe, il ne peut pas participer avec une autre équipe pour le même événement

4. **Dates d'inscription**
   - Les inscriptions ne sont possibles que si :
     - L'événement est en statut `open`
     - La date actuelle est entre `registrationStartDate` et `registrationEndDate`

5. **Impossible d'annuler une inscription si l'événement est en cours**
   - Si l'événement est en statut `in_progress`, l'inscription ne peut pas être annulée

6. **Impossible de modifier un événement en cours**
   - Un événement avec le statut `in_progress` ne peut pas être modifié (même par un admin)

#### Membres et Équipes pendant les événements
1. **Impossible de retirer un membre participant à un événement en cours**
   - Si un membre participe à un événement avec le statut `in_progress`, il ne peut pas être retiré de l'équipe

2. **Impossible de quitter l'équipe si participation à un événement en cours**
   - Un membre (y compris le capitaine) ne peut pas quitter l'équipe s'il participe à un événement en cours
   - Le capitaine doit d'abord transférer son rôle s'il veut quitter

3. **Transfert de capitaine**
   - Le capitaine peut transférer son rôle à un autre membre
   - Le nouveau capitaine doit être membre de l'équipe
   - Impossible de transférer à soi-même

#### Matchs
1. **Validation des matchs**
   - Un match passe en `pending_validation` quand un score est enregistré et qu'un gagnant est déterminé
   - Pour valider un match :
     - Un admin peut valider seul
     - Les deux capitaines des équipes participantes doivent valider
   - Un utilisateur ne peut valider qu'une seule fois par match

2. **Mise à jour du score**
   - Seuls les admins et les capitaines des équipes participantes peuvent mettre à jour le score
   - Le gagnant est déterminé automatiquement selon le score

3. **Statut des matchs**
   - Les statuts possibles : `upcoming`, `in_progress`, `finished`, `pending_validation`, `cancelled`
   - Quand un match passe en `in_progress`, `actualStartTime` est automatiquement défini
   - Quand un match passe en `finished`, `actualEndTime` est automatiquement défini

#### Demandes d'adhésion
1. **Une seule demande en attente par équipe**
   - Un utilisateur ne peut avoir qu'une seule demande en statut `pending` pour une équipe donnée

2. **Impossible de demander à rejoindre une équipe inactive**
   - Seules les équipes avec le statut `active` peuvent recevoir des demandes

3. **Annulation de demande**
   - Seul l'auteur de la demande peut l'annuler

### ⚠️ Messages d'erreur courants

| Message d'erreur | Cause | Solution |
|-----------------|-------|----------|
| "Vous êtes déjà capitaine d'une équipe active pour ce jeu" | Tentative de créer une deuxième équipe pour le même jeu | Supprimer ou quitter l'équipe existante |
| "Vous êtes déjà membre d'une équipe active pour ce jeu" | Tentative de rejoindre une équipe alors qu'on est déjà membre d'une autre | Quitter l'équipe existante |
| "Les inscriptions ne sont pas encore ouvertes" | Date actuelle < `registrationStartDate` | Attendre l'ouverture des inscriptions |
| "Les inscriptions sont fermées" | Date actuelle > `registrationEndDate` | L'événement n'accepte plus d'inscriptions |
| "Le format X nécessite exactement Y joueurs" | Nombre de membres sélectionnés ne correspond pas au format | Sélectionner le bon nombre de membres |
| "Membre déjà inscrit dans une autre équipe pour cet événement" | Un membre participe déjà à cet événement avec une autre équipe | Choisir d'autres membres |
| "Impossible d'annuler une inscription pour un événement en cours" | Tentative d'annuler alors que l'événement est `in_progress` | Attendre la fin de l'événement |
| "Impossible de retirer un membre participant à un événement en cours" | Tentative de retirer un membre qui participe à un événement `in_progress` | Attendre la fin de l'événement |
| "Impossible de quitter l'équipe si vous participez à un événement en cours" | Tentative de quitter alors qu'on participe à un événement `in_progress` | Attendre la fin de l'événement ou transférer le rôle de capitaine |
| "Impossible de modifier un événement en cours" | Tentative de modifier un événement avec le statut `in_progress` | Attendre la fin de l'événement |

---

## Authentification

### Inscription

**POST** `/api/auth/sign-up`

```json
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "gamertag": "JohnDoe"
}
```

**Réponse:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "_id": "...",
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "gamertag": "JohnDoe",
      "role": "member"
    }
  }
}
```

### Connexion

**POST** `/api/auth/sign-in`

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Réponse:** Identique à l'inscription

### Utilisation du token

Toutes les routes privées nécessitent un header d'authentification :

```
Authorization: Bearer <token>
```

### Déconnexion

**POST** `/api/auth/sign-out`

Requiert l'authentification. Le token est révoqué côté serveur.

---

## Routes API

### 🔐 Authentification

| Méthode | Route | Description | Accès |
|---------|-------|-------------|-------|
| POST | `/api/auth/sign-up` | Inscription | Public |
| POST | `/api/auth/sign-in` | Connexion | Public |
| POST | `/api/auth/sign-out` | Déconnexion | Privé |

---

### 👤 Utilisateurs

| Méthode | Route | Description | Accès |
|---------|-------|-------------|-------|
| GET | `/api/users` | Liste tous les utilisateurs | Admin |
| GET | `/api/users/me` | Profil de l'utilisateur connecté | Privé |
| GET | `/api/users/:id` | Détails d'un utilisateur | Privé |
| PUT | `/api/users/me` | Modifier son profil | Privé |
| DELETE | `/api/users/me` | Supprimer son compte | Privé |
| PUT | `/api/users/:id` | Modifier un utilisateur | Admin |
| PUT | `/api/users/:id/role` | Modifier le rôle d'un utilisateur | Admin |
| DELETE | `/api/users/:id` | Supprimer un utilisateur | Admin |

**Exemple - Modifier son profil:**

```json
PUT /api/users/me
{
  "firstName": "Jane",
  "lastName": "Smith",
  "gamertag": "JaneSmith",
  "twitchUsername": "janesmith"
}
```

---

### 🎮 Jeux (Games)

| Méthode | Route | Description | Accès |
|---------|-------|-------------|-------|
| GET | `/api/games` | Liste tous les jeux actifs | Public |
| GET | `/api/games/:id` | Détails d'un jeu | Public |
| POST | `/api/games` | Créer un jeu | Admin |
| PUT | `/api/games/:id` | Modifier un jeu | Admin |
| DELETE | `/api/games/:id` | Supprimer un jeu | Admin |

**Exemple - Créer un jeu:**

```json
POST /api/games
{
  "name": "Valorant",
  "description": "Jeu de tir tactique",
  "logo": "https://example.com/valorant-logo.png",
  "rules": "Règles du tournoi...",
  "formats": ["5v5"]
}
```

---

### 👥 Équipes (Teams)

| Méthode | Route | Description | Accès |
|---------|-------|-------------|-------|
| GET | `/api/teams` | Liste toutes les équipes (filtres: `game`, `status`) | Public |
| GET | `/api/teams/:id` | Détails d'une équipe | Public |
| POST | `/api/teams` | Créer une équipe (utilisateur devient captain) | Privé |
| PUT | `/api/teams/:id` | Modifier équipe | Captain/Admin |
| DELETE | `/api/teams/:id` | Supprimer équipe | Captain/Admin |
| POST | `/api/teams/:id/invite` | Inviter un utilisateur | Captain |
| DELETE | `/api/teams/:id/members/:userId` | Retirer un membre | Captain |
| POST | `/api/teams/:id/transfer-captain` | Transférer le rôle de capitaine | Captain |
| POST | `/api/teams/:id/leave` | Quitter l'équipe | Member (non-captain) |

**Exemple - Créer une équipe:**

```json
POST /api/teams
{
  "name": "Team Alpha",
  "game": "game_id_here",
  "description": "Équipe compétitive",
  "maxMembers": 5
}
```

**Filtres disponibles pour GET /api/teams:**
- `?game=<gameId>` - Filtrer par jeu
- `?status=active` - Filtrer par statut (active, inactive)

**Règles de validation:**
- Un utilisateur ne peut être capitaine que d'une seule équipe active par jeu
- Un utilisateur ne peut être membre que d'une seule équipe active par jeu
- Impossible de créer une équipe pour un jeu inactif
- Les admins ne peuvent pas créer ou rejoindre d'équipes
- Le capitaine est automatiquement ajouté aux membres
- Impossible de retirer un membre participant à un événement en cours
- Impossible de quitter l'équipe si on participe à un événement en cours

---

### 📝 Demandes d'Équipe (Team Requests)

| Méthode | Route | Description | Accès |
|---------|-------|-------------|-------|
| GET | `/api/team-requests` | Liste des demandes (filtres: `team`, `user`, `status`) | Privé |
| GET | `/api/team-requests/:id` | Détails d'une demande | Privé |
| POST | `/api/team-requests` | Créer une demande (rejoindre équipe) | Privé |
| PUT | `/api/team-requests/:id/accept` | Accepter demande | Captain |
| PUT | `/api/team-requests/:id/reject` | Rejeter demande | Captain |
| DELETE | `/api/team-requests/:id` | Annuler sa demande | Auteur |

**Exemple - Créer une demande:**

```json
POST /api/team-requests
{
  "team": "team_id_here"
}
```

---

### 🎯 Événements (Events)

| Méthode | Route | Description | Accès |
|---------|-------|-------------|-------|
| GET | `/api/events` | Liste tous les événements (filtres: `game`, `status`, `upcoming`) | Public |
| GET | `/api/events/:id` | Détails d'un événement | Public |
| GET | `/api/events/nearby` | Événements à proximité (query: `lat`, `long`, `distance`) | Privé |
| POST | `/api/events` | Créer un événement | Admin |
| PUT | `/api/events/:id` | Modifier événement | Admin |
| DELETE | `/api/events/:id` | Supprimer événement | Admin |
| POST | `/api/events/:id/generate-bracket` | Générer bracket tournoi | Admin |
| GET | `/api/events/:id/bracket` | Obtenir le bracket/tournoi | Public |

**Exemple - Créer un événement:**

```json
POST /api/events
{
  "name": "Tournoi Valorant 2024",
  "game": "game_id_here",
  "startDate": "2024-12-15T10:00:00Z",
  "endDate": "2024-12-15T18:00:00Z",
  "registrationStartDate": "2024-12-01T00:00:00Z",
  "registrationEndDate": "2024-12-10T23:59:59Z",
  "format": "5v5",
  "rules": "Règles du tournoi...",
  "description": "Description de l'événement",
  "status": "open",
  "location": {
    "type": "online",
    "address": null,
    "coordinates": {
      "type": "Point",
      "coordinates": [0, 0]
    }
  },
  "maxTeams": 16
}
```

**Statuts d'événement:**
- `draft` - Brouillon (non visible publiquement)
- `open` - Inscriptions ouvertes
- `registration_closed` - Inscriptions fermées
- `in_progress` - En cours (ne peut pas être modifié)
- `completed` - Terminé
- `cancelled` - Annulé

**Filtres disponibles pour GET /api/events:**
- `?game=<gameId>` - Filtrer par jeu
- `?status=<status>` - Filtrer par statut
- `?upcoming=true` - Événements à venir (draft, open, registration_closed, in_progress)

---

### 📋 Inscriptions aux Événements (Event Registrations)

| Méthode | Route | Description | Accès |
|---------|-------|-------------|-------|
| GET | `/api/event-registrations` | Liste des inscriptions (filtres: `event`, `team`) | Privé |
| GET | `/api/event-registrations/:id` | Détails d'une inscription | Privé |
| GET | `/api/event-registrations/event/:eventId` | Inscriptions d'un événement | Privé |
| GET | `/api/event-registrations/team/:teamId` | Inscriptions d'une équipe | Privé |
| POST | `/api/event-registrations` | Inscrire une équipe à un événement | Captain |
| PUT | `/api/event-registrations/:id` | Modifier inscription | Captain |
| DELETE | `/api/event-registrations/:id` | Annuler inscription | Captain |

**Exemple - Inscrire une équipe:**

```json
POST /api/event-registrations
{
  "event": "event_id_here",
  "team": "team_id_here",
  "participatingMembers": ["member_id_1", "member_id_2", "member_id_3"]
}
```

**Règles de validation:**
- Seul le capitaine peut inscrire son équipe
- L'événement doit être en statut `open`
- Les dates d'inscription doivent être respectées (`registrationStartDate` ≤ maintenant ≤ `registrationEndDate`)
- L'équipe doit avoir exactement le nombre de membres requis selon le format (ex: 5v5 = 5 membres, le capitaine est inclus automatiquement)
- Un membre ne peut participer qu'à une seule équipe par événement
- Impossible d'annuler si l'événement est `in_progress`
- Tous les membres sélectionnés doivent faire partie de l'équipe

---

### 🎮 Matchs (Matches)

| Méthode | Route | Description | Accès |
|---------|-------|-------------|-------|
| GET | `/api/matches` | Liste tous les matchs (filtres: `event`, `status`, `team`) | Public |
| GET | `/api/matches/:id` | Détails d'un match | Public |
| GET | `/api/matches/event/:eventId` | Matchs d'un événement | Public |
| GET | `/api/matches/team/:teamId` | Matchs d'une équipe | Public |
| PUT | `/api/matches/:id/status` | Mettre à jour statut | Admin/Captain |
| PUT | `/api/matches/:id/score` | Mettre à jour score | Admin/Captain |
| POST | `/api/matches/:id/validate` | Valider résultat | Admin/Captain |

**Exemple - Mettre à jour le statut:**

```json
PUT /api/matches/:id/status
{
  "status": "in_progress"
}
```

**Statuts de match:**
- `upcoming` - À venir
- `in_progress` - En cours
- `finished` - Terminé
- `pending_validation` - En attente de validation
- `cancelled` - Annulé

**Exemple - Mettre à jour le score:**

```json
PUT /api/matches/:id/score
{
  "score": {
    "team1": 16,
    "team2": 12
  }
}
```

**Règles de validation:**
- Un admin peut valider seul
- Les deux capitaines doivent valider pour que le match soit validé
- Le gagnant est déterminé automatiquement selon le score
- Si le match est `in_progress` et qu'un score est enregistré, il passe en `pending_validation`

---

### 🔔 Notifications

| Méthode | Route | Description | Accès |
|---------|-------|-------------|-------|
| GET | `/api/notifications` | Liste notifications utilisateur | Privé |
| GET | `/api/notifications/:id` | Détails d'une notification | Privé |
| GET | `/api/notifications/unread-count` | Nombre de non lues | Privé |
| PUT | `/api/notifications/:id/read` | Marquer comme lue | Privé |
| PUT | `/api/notifications/read-all` | Marquer toutes comme lues | Privé |
| DELETE | `/api/notifications/:id` | Supprimer notification | Privé |

**Types de notifications:**
- `team_request` - Demande d'adhésion à une équipe
- `team_request_accepted` - Demande acceptée
- `team_request_rejected` - Demande rejetée
- `team_invitation` - Invitation à rejoindre une équipe
- `event_registration_created` - Équipe inscrite à un événement
- `event_registration_cancelled` - Inscription annulée
- `event_started` - Événement commencé
- `match_starting_soon` - Match qui commence bientôt
- `match_status_changed` - Statut de match modifié
- `match_score_updated` - Score de match mis à jour

---

### 📺 Twitch (Intégration)

| Méthode | Route | Description | Accès |
|---------|-------|-------------|-------|
| GET | `/api/twitch/user/:username` | Infos utilisateur Twitch | Public |
| GET | `/api/twitch/stream/:username` | Statut stream Twitch | Public |
| GET | `/api/twitch/followers/:username` | Followers Twitch | Public |
| GET | `/api/twitch/games` | Jeux populaires Twitch | Public |

---

## Use Cases par Rôle

### 👤 Member (Membre)

#### 1. S'inscrire et se connecter
1. POST `/api/auth/sign-up` avec email, password, firstName, lastName, gamertag
2. POST `/api/auth/sign-in` pour se connecter
3. Stocker le token JWT reçu

#### 2. Créer une équipe
1. GET `/api/games` pour voir les jeux disponibles
2. POST `/api/teams` avec les détails de l'équipe
3. L'utilisateur devient automatiquement `captain`

#### 3. Rejoindre une équipe
1. GET `/api/teams` pour voir les équipes disponibles
2. GET `/api/teams/:id` pour voir les détails
3. POST `/api/team-requests` avec `team: teamId`
4. Attendre que le capitaine accepte

#### 4. Consulter les événements
1. GET `/api/events` pour voir tous les événements
2. GET `/api/events?upcoming=true` pour les événements à venir
3. GET `/api/events/:id` pour les détails d'un événement

#### 5. Voir les matchs
1. GET `/api/matches` pour voir tous les matchs
2. GET `/api/matches/event/:eventId` pour les matchs d'un événement
3. GET `/api/matches/team/:teamId` pour les matchs de son équipe

---

### 👨‍✈️ Captain (Capitaine)

#### 1. Gérer son équipe
1. PUT `/api/teams/:id` pour modifier l'équipe
2. POST `/api/teams/:id/invite` pour inviter un membre
3. DELETE `/api/teams/:id/members/:userId` pour retirer un membre
4. POST `/api/teams/:id/transfer-captain` pour transférer le rôle

#### 2. Gérer les demandes d'adhésion
1. GET `/api/team-requests?team=<teamId>` pour voir les demandes
2. PUT `/api/team-requests/:id/accept` pour accepter
3. PUT `/api/team-requests/:id/reject` pour rejeter

#### 3. Inscrire son équipe à un événement
1. GET `/api/events` pour voir les événements disponibles
2. GET `/api/events/:id` pour vérifier les détails et le statut
3. POST `/api/event-registrations` avec:
   - `event`: eventId
   - `team`: teamId
   - `participatingMembers`: [liste des IDs des membres participants]
4. Tous les membres participants reçoivent une notification

#### 4. Gérer les inscriptions
1. GET `/api/event-registrations?team=<teamId>` pour voir les inscriptions
2. PUT `/api/event-registrations/:id` pour modifier (changer les membres participants)
3. DELETE `/api/event-registrations/:id` pour annuler (si l'événement n'est pas `in_progress`)

#### 5. Gérer les matchs de son équipe
1. GET `/api/matches/team/:teamId` pour voir les matchs
2. PUT `/api/matches/:id/status` pour mettre à jour le statut
3. PUT `/api/matches/:id/score` pour mettre à jour le score
4. POST `/api/matches/:id/validate` pour valider le résultat

---

### 👑 Admin (Administrateur)

#### 1. Gérer les jeux
1. POST `/api/games` pour créer un jeu
2. PUT `/api/games/:id` pour modifier
3. DELETE `/api/games/:id` pour supprimer

#### 2. Gérer les événements
1. POST `/api/events` pour créer un événement
2. PUT `/api/events/:id` pour modifier (sauf si `in_progress`)
3. DELETE `/api/events/:id` pour supprimer (supprime aussi les inscriptions)
4. GET `/api/event-registrations/event/:eventId` pour voir toutes les inscriptions

#### 3. Générer les brackets
1. POST `/api/events/:id/generate-bracket` pour générer le bracket
2. GET `/api/events/:id/bracket` pour voir le bracket
3. POST `/api/events/:id/generate-bracket` à nouveau pour régénérer

#### 4. Gérer les matchs
1. PUT `/api/matches/:id/status` pour modifier le statut
2. PUT `/api/matches/:id/score` pour modifier le score
3. POST `/api/matches/:id/validate` pour valider (peut valider seul)

#### 5. Gérer les utilisateurs
1. GET `/api/users` pour voir tous les utilisateurs
2. PUT `/api/users/:id/role` pour modifier le rôle
3. DELETE `/api/users/:id` pour supprimer un utilisateur

---

## Modèles de Données

### User (Utilisateur)
```javascript
{
  _id: ObjectId,
  email: String (unique, required),
  password: String (hashed, required),
  firstName: String (required),
  lastName: String (required),
  gamertag: String (unique, required),
  role: String (enum: ['member', 'captain', 'admin'], default: 'member'),
  twitchUsername: String (optional),
  preferences: {
    favoriteGames: [ObjectId],
    notificationSettings: Object
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

### Game (Jeu)
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

### Team (Équipe)
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

### Event (Événement)
```javascript
{
  _id: ObjectId,
  name: String (required),
  game: ObjectId (ref: 'Game', required),
  startDate: Date (required),
  endDate: Date (required),
  registrationStartDate: Date (required),
  registrationEndDate: Date (required),
  format: String (enum: ['1v1', '2v2', '3v3', '4v4', '5v5', 'BATTLE_ROYALE'], required),
  rules: String,
  description: String,
  status: String (enum: ['draft', 'open', 'registration_closed', 'in_progress', 'completed', 'cancelled'], default: 'draft'),
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

### EventRegistration (Inscription)
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

### Match (Match)
```javascript
{
  _id: ObjectId,
  event: ObjectId (ref: 'Event', required),
  teams: [ObjectId] (ref: 'Team', required),
  scheduledTime: Date (required),
  actualStartTime: Date,
  actualEndTime: Date,
  status: String (enum: ['upcoming', 'in_progress', 'finished', 'pending_validation', 'cancelled'], default: 'upcoming'),
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

---

## Workflows Principaux

### Workflow 1: Création d'une équipe et inscription à un événement

1. **User (Member) crée une équipe**
   - POST `/api/teams` → User devient `captain`

2. **Captain invite des membres**
   - POST `/api/teams/:id/invite` → Les membres reçoivent des notifications

3. **Captain inscrit l'équipe à un événement**
   - POST `/api/event-registrations` avec les membres participants
   - Tous les membres participants reçoivent une notification

4. **Admin génère le bracket**
   - POST `/api/events/:id/generate-bracket`
   - Les matchs sont créés automatiquement dans le bracket

### Workflow 2: Gestion d'un match

1. **Match créé automatiquement lors de la génération du bracket**
   - Statut initial: `upcoming`

2. **Captain ou Admin démarre le match**
   - PUT `/api/matches/:id/status` avec `status: "in_progress"`
   - `actualStartTime` est automatiquement défini

3. **Captain ou Admin met à jour le score**
   - PUT `/api/matches/:id/score` avec les scores
   - Le gagnant est déterminé automatiquement
   - Le statut passe à `pending_validation`

4. **Validation du résultat**
   - POST `/api/matches/:id/validate` par les deux capitaines ou un admin
   - Si les deux capitaines valident (ou un admin), le statut passe à `finished`

### Workflow 3: Demande d'adhésion à une équipe

1. **Member crée une demande**
   - POST `/api/team-requests` avec `team: teamId`
   - Le captain reçoit une notification

2. **Captain accepte ou rejette**
   - PUT `/api/team-requests/:id/accept` → Le member reçoit une notification et est ajouté à l'équipe
   - PUT `/api/team-requests/:id/reject` → Le member reçoit une notification

---





## Scripts d'initialisation

Le projet contient des scripts pour initialiser la base de données :

- `scripts/initDefaultAdmin.js` - Crée un admin par défaut
- `scripts/initDefaultGames.js` - Crée des jeux de test
- `scripts/initDefaultUsers.js` - Crée des utilisateurs, équipes et membres
- `scripts/initDefaultEvents.js` - Crée des événements de test avec brackets

Ces scripts sont exécutés automatiquement au démarrage du serveur.


# L4M-eSports
