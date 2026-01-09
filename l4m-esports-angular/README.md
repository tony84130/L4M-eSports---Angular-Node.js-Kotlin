# L4M Esports Angular

Application web développée avec Angular pour la plateforme de gestion de club e-sports L4M Esports.

## 📋 Table des matières

- [Prérequis](#prérequis)
- [Installation](#installation)
- [Configuration](#configuration)
- [Lancement](#lancement)
- [Architecture](#architecture)
- [Structure du projet](#structure-du-projet)
- [Fonctionnalités](#fonctionnalités)
- [Technologies utilisées](#technologies-utilisées)

## 🔧 Prérequis

Avant de commencer, assurez-vous d'avoir installé :

- **Node.js** (version 18 ou supérieure)
- **npm** (généralement inclus avec Node.js)
- **Angular CLI** (version 17 ou supérieure)
- **Serveur backend** en cours d'exécution (voir [l4m-esports-server](../l4m-esports-server/README.md))

## 📦 Installation

1. **Cloner le projet** (si ce n'est pas déjà fait)
   ```bash
   git clone <repository-url>
   cd l4m-esports-angular
   ```

2. **Installer les dépendances**
   ```bash
   npm install
   ```

3. **Installer Angular CLI globalement** (si ce n'est pas déjà fait)
   ```bash
   npm install -g @angular/cli
   ```

## ⚙️ Configuration

### URL de l'API

L'application est configurée pour utiliser un proxy vers le serveur backend. Le fichier `proxy.conf.json` est déjà configuré :

```json
{
  "/api": {
    "target": "http://localhost:3000",
    "secure": false,
    "changeOrigin": true
  }
}
```

Pour modifier l'URL du serveur backend, éditez `proxy.conf.json`.

### Variables d'environnement

Créez un fichier `src/environments/environment.ts` :

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:3000/api'
};
```

## 🚀 Lancement

### 1. Démarrer le serveur backend

Assurez-vous que le serveur backend est en cours d'exécution (voir [l4m-esports-server](../l4m-esports-server/README.md)).

### 2. Démarrer l'application

```bash
npm start
```

Ou avec Angular CLI :

```bash
ng serve --proxy-config proxy.conf.json
```

L'application sera accessible sur `http://localhost:4200/`.

### 3. Build de production

```bash
npm run build
```

Les fichiers de production seront générés dans le dossier `dist/`.

## 🏗️ Architecture

L'application suit une architecture **feature-first** avec composants standalone :

- **Feature-first** : Organisation par fonctionnalité métier
- **Standalone components** : Composants autonomes (pas de NgModules)
- **Signals** : Gestion réactive de l'état
- **Lazy loading** : Chargement à la demande des features
- **Services + Signals** : État local par feature
- **API centralisée** : Client REST/WS typé dans `api/`

### Principes architecturaux

- **Découplage par domaine** : Chaque feature encapsule sa logique
- **Lazy-loading par feature** : Chargement à la demande pour optimiser les performances
- **Base PWA** : Prêt pour Progressive Web App
- **Temps réel** : Service Socket.io pour la synchronisation en temps réel
- **Synchronisation automatique** : Mise à jour automatique de l'interface lors de modifications

## 📁 Structure du projet

```
l4m-esports-angular/
├── src/
│   ├── app/
│   │   ├── core/                    # Services transverses
│   │   │   ├── guards/              # Guards d'authentification
│   │   │   │   └── auth.guard.ts
│   │   │   ├── interceptors/        # Intercepteurs HTTP
│   │   │   │   └── auth.interceptor.ts
│   │   │   ├── services/            # Services globaux
│   │   │   │   ├── auth.service.ts
│   │   │   │   ├── users.service.ts
│   │   │   │   ├── games.service.ts
│   │   │   │   ├── teams.service.ts
│   │   │   │   ├── events.service.ts
│   │   │   │   ├── matches.service.ts
│   │   │   │   ├── notifications.service.ts
│   │   │   │   ├── twitch.service.ts
│   │   │   │   └── ai.service.ts
│   │   │   └── models.ts             # Modèles partagés
│   │   ├── shared/                  # Composants réutilisables
│   │   ├── api/                     # Client API centralisé
│   │   ├── features/                # Features (lazy-loaded)
│   │   │   ├── auth/
│   │   │   │   ├── login-page.component.ts
│   │   │   │   └── sign-up-page.component.ts
│   │   │   ├── dashboard/
│   │   │   │   └── dashboard-page.component.ts
│   │   │   ├── games/
│   │   │   │   └── games-page.component.ts
│   │   │   ├── teams/
│   │   │   │   └── teams-page.component.ts
│   │   │   ├── events/
│   │   │   │   └── events-page.component.ts
│   │   │   ├── matches/
│   │   │   │   └── matches-page.component.ts
│   │   │   ├── notifications/
│   │   │   │   └── notifications-page.component.ts
│   │   │   ├── profile/
│   │   │   │   └── profile-page.component.ts
│   │   │   └── support/
│   │   │       └── ai-help-widget.component.ts
│   │   ├── layout/
│   │   │   └── app-layout.component.ts
│   │   ├── app.config.ts             # Configuration principale
│   │   └── app.routes.ts             # Routes
│   └── assets/                       # Assets statiques
├── angular.json
├── package.json
├── proxy.conf.json                    # Configuration proxy
└── README.md
```

## ✨ Fonctionnalités

### Authentification
- ✅ Inscription
- ✅ Connexion
- ✅ Déconnexion
- ✅ Gestion du profil utilisateur

### Gestion des jeux
- ✅ Liste des jeux
- ✅ Détails d'un jeu
- ✅ Création/modification (Admin uniquement)

### Gestion des équipes
- ✅ Liste des équipes
- ✅ Détails d'une équipe
- ✅ Création/modification d'équipe (avec filtrage des jeux disponibles)
- ✅ Gestion des membres
- ✅ Transfert de capitaine
- ✅ Dissolution d'équipe (Admin/Captain)

### Gestion des événements
- ✅ Liste des événements avec filtres (à venir, tous)
- ✅ Détails d'un événement
- ✅ Création/modification/suppression (Admin uniquement)
- ✅ Génération/régénération de bracket (Admin uniquement)
- ✅ Mise à jour automatique du statut (basé sur les dates et les matchs)
- ✅ Affichage du statut traduit en français

### Gestion des matchs
- ✅ Liste des matchs avec filtres par statut
- ✅ Modification du statut et du score (Admin uniquement)
- ✅ Validation des résultats (Admin uniquement)
- ✅ Transition automatique : in_progress → pending_validation → finished
- ✅ Détails d'un match
- ✅ Mise à jour (Admin uniquement)

### Notifications
- ✅ Liste des notifications
- ✅ Marquer comme lue
- ✅ Compteur de non lues

### Intégration Twitch
- ✅ Liaison du compte Twitch
- ✅ Affichage des statistiques

### Assistant IA
- ✅ Widget d'assistant IA
- ✅ Interface de chat contextuelle

## 📦 Technologies utilisées

### Core Angular
- **Angular** 17+ : Framework principal
- **TypeScript** : Langage de programmation
- **RxJS** : Programmation réactive

### Architecture
- **Standalone Components** : Composants autonomes
- **Signals** : Gestion réactive de l'état
- **Lazy Loading** : Chargement à la demande

### UI/UX
- **Angular Material** (si utilisé) : Composants UI
- **CSS/SCSS** : Styles

### HTTP
- **HttpClient** : Client HTTP Angular
- **Interceptors** : Intercepteurs HTTP pour l'authentification

### Temps réel
- **Socket.io Client** : Synchronisation en temps réel avec le serveur

## 🔐 Authentification

L'application utilise JWT (JSON Web Tokens) pour l'authentification :

1. **Sign Up / Sign In** : Le token JWT est reçu du serveur
2. **Stockage** : Le token est stocké dans le localStorage ou sessionStorage
3. **Intercepteur** : Le token est automatiquement ajouté aux requêtes via `AuthInterceptor`
4. **Guard** : Les routes protégées utilisent `AuthGuard`
5. **Sign Out** : Le token est supprimé lors de la déconnexion

## 🧪 Tests

### Lancer les tests unitaires

```bash
npm test
```

Ou avec Angular CLI :

```bash
ng test
```

### Lancer les tests e2e

```bash
ng e2e
```

## 📝 Scripts disponibles

```bash
# Démarrer le serveur de développement
npm start
# ou
ng serve --proxy-config proxy.conf.json

# Build de production
npm run build
# ou
ng build

# Lancer les tests
npm test
# ou
ng test

# Build en mode watch
npm run watch
# ou
ng build --watch --configuration development
```

## 🐛 Dépannage

### L'application ne se connecte pas au serveur

1. Vérifiez que le serveur backend est en cours d'exécution
2. Vérifiez la configuration du proxy dans `proxy.conf.json`
3. Vérifiez que le serveur accepte les connexions depuis `http://localhost:4200`

### Erreur de build

- Supprimez `node_modules` et `package-lock.json`
- Réinstallez les dépendances : `npm install`
- Nettoyez le cache : `ng cache clean`

### Erreur de CORS

- Vérifiez que le serveur backend autorise les requêtes depuis `http://localhost:4200`
- Vérifiez la configuration CORS dans le serveur backend

## 📚 Documentation

- [Architecture détaillée](ARCHITECTURE.md) - Documentation complète de l'architecture
- [Angular Documentation](https://angular.io/docs) - Documentation officielle Angular
- [Angular CLI](https://angular.io/cli) - Documentation Angular CLI

## 🔄 Prochaines étapes

Les fonctionnalités suivantes peuvent être ajoutées :

- **WebSockets** : Notifications en temps réel
- **PWA** : Support Progressive Web App
- **Mode offline** : Support du mode hors ligne
- **Push notifications** : Notifications push natives
- **Tests e2e** : Tests end-to-end complets

## 👤 Auteurs

**Équipe L4M Esports**

Ce projet a été développé dans le cadre du cours IFT717 - Projet session.

**Date de création** : 2025

Pour plus d'informations sur le projet, consultez le [Rapport de projet](../RAPPORT_PROJET.md).
