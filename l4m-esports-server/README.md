# L4M Esports Server

Backend API REST pour la plateforme de gestion de club e-sports L4M Esports. Développé avec Node.js, Express, MongoDB et Redis.

## 📋 Table des matières

- [Prérequis](#prérequis)
- [Installation](#installation)
- [Configuration](#configuration)
- [Lancement](#lancement)
- [Structure du projet](#structure-du-projet)
- [Routes disponibles](#routes-disponibles)
- [Tests](#tests)
- [Architecture](#architecture)
- [Documentation API](#documentation-api)

## 🔧 Prérequis

Avant de commencer, assurez-vous d'avoir installé :

- **Node.js** (version 18 ou supérieure)
- **npm** (généralement inclus avec Node.js)
- **Docker** et **Docker Compose** (pour Redis)
- **MongoDB** (local ou MongoDB Atlas)

## 📦 Installation

1. **Cloner le projet** (si ce n'est pas déjà fait)
   ```bash
   git clone <repository-url>
   cd l4m-esports-server
   ```

2. **Installer les dépendances**
   ```bash
   npm install
   ```

3. **Créer le fichier `.env`**
   ```bash
   cp .env.example .env
   ```
   Ou créez manuellement un fichier `.env` à la racine du projet.

## ⚙️ Configuration

Créez un fichier `.env` à la racine du projet en copiant le fichier `.env.example` :

```bash
cp .env.example .env
```

Puis modifiez les valeurs selon votre configuration. Le fichier `.env.example` contient toutes les variables nécessaires avec des valeurs par défaut.

### Variables d'environnement requises

| Variable | Description | Exemple |
|----------|-------------|---------|
| `PORT` | Port du serveur | `3000` |
| `MONGODB_URI` | URI de connexion MongoDB | `mongodb://localhost:27017/l4m-esports` |
| `REDIS_URL` | URL de connexion Redis | `redis://localhost:6379` |
| `JWT_SECRET` | Clé secrète pour JWT | `your-secret-key` |

### Variables optionnelles

| Variable | Description | Défaut |
|----------|-------------|--------|
| `NODE_ENV` | Environnement d'exécution | `development` |
| `JWT_EXPIRES_IN` | Durée de validité du token | `24h` |
| `CLIENT_URL` | URL du client (pour CORS) | `http://localhost:3000` |
| `OPENAI_API_KEY` | Clé API OpenAI pour l'assistant IA | - |
| `TWITCH_CLIENT_ID` | ID client Twitch API | - |
| `TWITCH_CLIENT_SECRET` | Secret client Twitch API | - |
| `ADMIN_EMAIL` | Email de l'administrateur | - |
| `ADMIN_PASSWORD` | Mot de passe de l'administrateur | - |
| `ADMIN_FIRST_NAME` | Prénom de l'administrateur | `Admin` |
| `ADMIN_LAST_NAME` | Nom de l'administrateur | `User` |
| `ADMIN_GAMERTAG` | Gamertag de l'administrateur | `admin` |

### Configuration MongoDB

**Option 1 : MongoDB local**
```env
MONGODB_URI=mongodb://localhost:27017/l4m-esports
```

**Option 2 : MongoDB Atlas**
```env
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/l4m-esports?retryWrites=true&w=majority
```

### Configuration CORS

Le serveur utilise `CLIENT_URL` pour configurer CORS. Par défaut :
```env
CLIENT_URL=http://localhost:3000
```

Pour autoriser plusieurs origines, vous pouvez modifier le code dans `app.js` ou utiliser une variable d'environnement personnalisée.

## 🚀 Lancement

### 1. Démarrer Redis avec Docker

Redis est nécessaire pour la gestion des sessions et la révocation des tokens.

```bash
docker-compose up -d
```

Cela démarre Redis dans un conteneur Docker sur le port `6379`.

Pour arrêter Redis :
```bash
docker-compose down
```

Pour voir les logs Redis :
```bash
docker-compose logs -f redis-project-session
```

### 2. Démarrer MongoDB

**Option A : MongoDB local**
- Assurez-vous que MongoDB est installé et en cours d'exécution
- MongoDB doit être accessible à l'URI spécifiée dans `.env`

**Option B : MongoDB Atlas**
- Créez un cluster sur [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
- Récupérez l'URI de connexion et mettez-la dans `.env`

### 3. Démarrer le serveur

Le serveur démarre avec rechargement automatique (nodemon) :
```bash
npm start
```

Le serveur démarre sur `http://localhost:3000` (ou le port spécifié dans `.env`).

**Note :** Si les variables `ADMIN_EMAIL` et `ADMIN_PASSWORD` sont définies dans `.env`, un compte administrateur sera automatiquement créé au démarrage (s'il n'existe pas déjà). Les scripts d'initialisation créent également des jeux, utilisateurs, équipes et événements de test.

### 4. Vérifier que tout fonctionne

Ouvrez votre navigateur ou utilisez curl :

```bash
# Health check
curl http://localhost:3000/health

# Message d'accueil
curl http://localhost:3000/
```

Vous devriez voir :
```json
{
  "status": "healthy",
  "timestamp": "2024-01-01T12:00:00.000Z"
}
```

## 📁 Structure du projet

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
│   ├── game.controller.js
│   ├── team.controller.js
│   ├── event.controller.js
│   ├── match.controller.js
│   └── ...
├── services/                 # Services (logique métier)
│   ├── auth.service.js
│   ├── user.service.js
│   ├── event.service.js
│   ├── match.service.js
│   ├── notification.service.js
│   ├── ai.service.js
│   ├── twitch.service.js
│   └── redis.service.js
├── middlewares/              # Middlewares Express
│   ├── auth.middleware.js   # Authentification JWT
│   ├── error.middleware.js  # Gestion des erreurs
│   └── validation.middleware.js
├── utils/                    # Utilitaires
│   └── errors.js            # Classes d'erreurs personnalisées
├── scripts/                  # Scripts d'initialisation
│   ├── initDefaultAdmin.js
│   ├── initDefaultGames.js
│   ├── initDefaultUsers.js
│   └── initDefaultEvents.js
├── tests/                    # Tests
│   ├── auth.test.js
│   ├── game.test.js
│   ├── team.test.js
│   └── ...
├── postman/                  # Collection Postman
│   ├── L4M_Esports_API.postman_collection.json
│   └── L4M_Esports_Local.postman_environment.json
├── docker-compose.yml         # Configuration Docker pour Redis
├── package.json
└── README.md
```

## 🛣️ Routes disponibles

### Authentification
- `POST /api/auth/sign-up` - Inscription
- `POST /api/auth/sign-in` - Connexion
- `POST /api/auth/sign-out` - Déconnexion

### Utilisateurs
- `GET /api/users` - Liste tous les utilisateurs (Admin)
- `GET /api/users/me` - Profil de l'utilisateur connecté
- `PUT /api/users/me` - Mettre à jour son profil
- `GET /api/users/:id` - Profil d'un utilisateur
- `PUT /api/users/:id` - Mettre à jour un utilisateur (Admin)
- `PUT /api/users/:id/role` - Changer le rôle (Admin)
- `DELETE /api/users/:id` - Supprimer un utilisateur (Admin)

### Jeux
- `GET /api/games` - Liste tous les jeux
- `GET /api/games/:id` - Détails d'un jeu
- `POST /api/games` - Créer un jeu (Admin)
- `PUT /api/games/:id` - Modifier un jeu (Admin)
- `DELETE /api/games/:id` - Supprimer un jeu (Admin)

### Équipes
- `GET /api/teams` - Liste toutes les équipes (filtres: `game`, `status`)
- `GET /api/teams/:id` - Détails d'une équipe
- `POST /api/teams` - Créer une équipe
- `PUT /api/teams/:id` - Modifier équipe (Captain/Admin)
- `DELETE /api/teams/:id` - Supprimer équipe (Captain/Admin)
- `POST /api/teams/:id/invite` - Inviter un utilisateur (Captain)
- `DELETE /api/teams/:id/members/:userId` - Retirer un membre (Captain)
- `POST /api/teams/:id/transfer-captain` - Transférer le rôle de capitaine (Captain)
- `POST /api/teams/:id/leave` - Quitter l'équipe (Member)

### Demandes d'équipe
- `GET /api/team-requests` - Liste des demandes (filtres: `team`, `user`, `status`)
- `GET /api/team-requests/:id` - Détails d'une demande
- `POST /api/team-requests` - Créer une demande
- `PUT /api/team-requests/:id/accept` - Accepter demande (Captain)
- `PUT /api/team-requests/:id/reject` - Rejeter demande (Captain)
- `DELETE /api/team-requests/:id` - Annuler demande

### Événements
- `GET /api/events` - Liste tous les événements (filtres: `game`, `status`, `upcoming`)
- `GET /api/events/:id` - Détails d'un événement
- `GET /api/events/:id/bracket` - Bracket d'un événement
- `GET /api/events/nearby` - Événements à proximité (query: `lat`, `long`, `distance`)
- `POST /api/events` - Créer un événement (Admin)
- `PUT /api/events/:id` - Modifier événement (Admin)
- `DELETE /api/events/:id` - Supprimer événement (Admin)
- `POST /api/events/:id/generate-bracket` - Générer bracket tournoi (Admin)

### Inscriptions aux événements
- `GET /api/event-registrations` - Liste des inscriptions (filtres: `event`, `team`)
- `GET /api/event-registrations/:id` - Détails d'une inscription
- `GET /api/event-registrations/event/:eventId` - Inscriptions d'un événement
- `POST /api/event-registrations` - Inscrire une équipe à un événement (Captain)
- `PUT /api/event-registrations/:id` - Modifier inscription (Captain/Admin)
- `DELETE /api/event-registrations/:id` - Annuler inscription (Captain)

### Matchs
- `GET /api/matches` - Liste tous les matchs (filtres: `event`, `status`, `team`)
- `GET /api/matches/:id` - Détails d'un match
- `GET /api/matches/event/:eventId` - Matchs d'un événement
- `GET /api/matches/team/:teamId` - Matchs d'une équipe
- `PUT /api/matches/:id/status` - Mettre à jour statut (Admin)
- `PUT /api/matches/:id/score` - Mettre à jour score (Admin)
- `PUT /api/matches/:id` - Mettre à jour match (Admin)
- `POST /api/matches/:id/validate` - Valider résultat (Admin)

### Notifications
- `GET /api/notifications` - Liste notifications utilisateur
- `GET /api/notifications/:id` - Détails d'une notification
- `GET /api/notifications/unread-count` - Nombre de non lues
- `PUT /api/notifications/:id/read` - Marquer comme lue
- `PUT /api/notifications/read-all` - Marquer toutes comme lues
- `DELETE /api/notifications/:id` - Supprimer notification

### Twitch
- `GET /api/twitch/user/:username` - Infos utilisateur Twitch

### IA
- `POST /api/ai/assist` - Poser une question à l'assistant IA

### WebSocket (Socket.io)
- **Connexion** : `io.connect('http://localhost:3000', { auth: { token } })`
- **Événements émis par le serveur** :
  - `user:created` - Nouvel utilisateur créé
  - `user:updated` - Utilisateur mis à jour
  - `user:deleted` - Utilisateur supprimé
  - `user:roleUpdated` - Rôle utilisateur mis à jour
  - `game:created` - Nouveau jeu créé
  - `game:updated` - Jeu mis à jour
  - `game:deleted` - Jeu supprimé
  - `team:created` - Nouvelle équipe créée
  - `team:updated` - Équipe mise à jour
  - `team:deleted` - Équipe supprimée
  - `team:captainTransferred` - Capitaine transféré
  - `event:created` - Nouvel événement créé
  - `event:updated` - Événement mis à jour
  - `event:deleted` - Événement supprimé
  - `match:updated` - Match mis à jour

### Health
- `GET /health` - Health check
- `GET /` - Message d'accueil

## 🧪 Tests

### Lancer les tests

```bash
# Tous les tests
npm test

# Tests en mode watch
npm run test:watch

# Tests avec couverture
npm run test:coverage
```

### Tests disponibles

- Tests d'authentification
- Tests de gestion des jeux
- Tests de gestion des équipes
- Tests de gestion des demandes d'adhésion
- Tests de gestion des utilisateurs

## 🧪 Tests avec Postman

Une collection Postman est disponible dans le dossier `postman/`.

### Import dans Postman

1. Ouvrez Postman
2. Cliquez sur **Import**
3. Importez les deux fichiers :
   - `postman/L4M_Esports_API.postman_collection.json`
   - `postman/L4M_Esports_Local.postman_environment.json`

### Configuration

1. Sélectionnez l'environnement **"L4M Esports - Local"** dans le menu déroulant
2. Les variables sont pré-configurées :
   - `base_url`: `http://localhost:3000`
   - `token`: Sera rempli automatiquement après sign-in/sign-up

### Utilisation

1. Exécutez `Auth > Sign Up` ou `Auth > Sign In`
2. Le token sera automatiquement sauvegardé dans la variable `token`
3. Toutes les autres requêtes utiliseront automatiquement ce token

Pour plus de détails, consultez `postman/README.md`.

## 🏗️ Architecture

### Séparation des responsabilités

- **Routes** : Définissent les endpoints et appliquent les middlewares
- **Controllers** : Gèrent les requêtes/réponses HTTP
- **Services** : Contiennent la logique métier
- **Models** : Définissent les schémas de données Mongoose
- **Middlewares** : Validation, authentification, gestion d'erreurs

### Flux de requête

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
    ↓
Émission Socket.io (si modification de données)
```

### Synchronisation en temps réel (Socket.io)

Le serveur émet automatiquement des événements Socket.io lors de modifications de données pour synchroniser tous les clients connectés :

- **Événements utilisateurs** : `user:created`, `user:updated`, `user:deleted`, `user:roleUpdated`
- **Événements jeux** : `game:created`, `game:updated`, `game:deleted`
- **Événements équipes** : `team:created`, `team:updated`, `team:deleted`, `team:captainTransferred`
- **Événements événements** : `event:created`, `event:updated`, `event:deleted`
- **Événements matchs** : `match:updated`

### Mise à jour automatique du statut des événements

Le serveur met automatiquement à jour le statut des événements :
- Basé sur les dates (inscriptions, début, fin)
- Basé sur la complétion des matchs (tous les matchs terminés → événement "completed")

### Gestion des erreurs

Toutes les erreurs sont gérées de manière centralisée par le middleware `error.middleware.js` :

- **Erreurs Mongoose** : ValidationError, CastError, DuplicateKey
- **Erreurs JWT** : Token invalide, expiré
- **Erreurs personnalisées** : NotFoundError, BadRequestError, ForbiddenError, etc.

## 🔐 Authentification

L'API utilise JWT (JSON Web Tokens) pour l'authentification :

1. **Sign Up / Sign In** : L'utilisateur reçoit un token JWT valide 24h
2. **Token stocké dans Redis** : Pour permettre la révocation
3. **Sign Out** : Le token est révoqué dans Redis
4. **Routes protégées** : Utilisent le middleware `authenticate`

### Format du token

Les tokens doivent être envoyés dans le header :
```
Authorization: Bearer <token>
```

## 📝 Scripts disponibles

```bash
# Démarrer le serveur (avec rechargement automatique via nodemon)
npm start

# Lancer les tests
npm test

# Tests en mode watch
npm run test:watch

# Tests avec couverture
npm run test:coverage
```

## 🐛 Dépannage

### Le serveur ne démarre pas

1. Vérifiez que MongoDB est en cours d'exécution
2. Vérifiez que Redis est démarré : `docker-compose ps`
3. Vérifiez les variables d'environnement dans `.env`
4. Vérifiez les logs : `npm start` affiche les erreurs dans la console

### Erreur de connexion MongoDB

- Vérifiez que MongoDB est installé et en cours d'exécution
- Vérifiez l'URI dans `.env`
- Pour MongoDB Atlas, vérifiez que votre IP est autorisée

### Erreur de connexion Redis

- Vérifiez que Docker est en cours d'exécution
- Démarrez Redis : `docker-compose up -d`
- Vérifiez les logs : `docker-compose logs redis-project-session`

### Token invalide

- Vérifiez que Redis est en cours d'exécution
- Le token expire après 24h, reconnectez-vous
- Vérifiez que le header `Authorization: Bearer <token>` est correct

## 📚 Documentation supplémentaire

- [Documentation complète du projet](../README.md) - Documentation complète de l'API, workflows, modèles de données
- [Collection Postman](postman/README.md) - Guide d'utilisation de Postman
- [Rapport de projet](../RAPPORT_PROJET.md) - Rapport complet du projet

## 👥 Contribution

Ce projet est développé en équipe.

## 👤 Auteurs

**Équipe L4M Esports**

Ce projet a été développé dans le cadre du cours IFT717 - Projet session.

**Date de création** : 2025

Pour plus d'informations sur le projet, consultez le [Rapport de projet](../RAPPORT_PROJET.md).
