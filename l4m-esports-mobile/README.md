# L4M Esports Mobile

Application Android native développée en Kotlin avec Jetpack Compose pour la plateforme de gestion de club e-sports L4M Esports.

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

- **Android Studio** (version Hedgehog ou supérieure)
- **JDK 11** ou supérieur
- **Android SDK** (API 24 minimum, API 36 cible)
- **Gradle** (inclus avec Android Studio)
- **Serveur backend** en cours d'exécution (voir [l4m-esports-server](../l4m-esports-server/README.md))

## 📦 Installation

1. **Cloner le projet** (si ce n'est pas déjà fait)
   ```bash
   git clone <repository-url>
   cd l4m-esports-mobile
   ```

2. **Ouvrir le projet dans Android Studio**
   - Ouvrez Android Studio
   - Sélectionnez "Open an Existing Project"
   - Naviguez vers le dossier `l4m-esports-mobile`

3. **Synchroniser Gradle**
   - Android Studio devrait automatiquement synchroniser les dépendances
   - Si ce n'est pas le cas, cliquez sur "Sync Project with Gradle Files"

4. **Configurer l'URL de l'API**
   - Modifiez `Constants.kt` pour pointer vers votre serveur backend
   - Voir la section [Configuration](#configuration)

## ⚙️ Configuration

### URL de l'API

L'application doit être configurée pour se connecter au serveur backend. Modifiez le fichier :

```
app/src/main/java/com/example/l4m_esports_mobile/util/Constants.kt
```

**Pour émulateur Android :**
```kotlin
const val BASE_URL = "http://10.0.2.2:3000"
```

**Pour appareil physique :**
```kotlin
// Remplacez par l'IP de votre machine sur le réseau local
const val BASE_URL = "http://192.168.1.100:3000"
```

**Pour serveur distant :**
```kotlin
const val BASE_URL = "https://api.l4m-esports.com"
```

### Permissions

L'application nécessite les permissions suivantes (déjà configurées dans `AndroidManifest.xml`) :

- `INTERNET` : Pour les appels API
- `ACCESS_FINE_LOCATION` : Pour la géolocalisation (événements en présentiel)
- `ACCESS_COARSE_LOCATION` : Pour la géolocalisation (événements en présentiel)

## 🚀 Lancement

### 1. Démarrer le serveur backend

Assurez-vous que le serveur backend est en cours d'exécution (voir [l4m-esports-server](../l4m-esports-server/README.md)).

### 2. Lancer l'application

**Option A : Émulateur Android**
1. Créez un émulateur Android (API 24 minimum)
2. Cliquez sur "Run" dans Android Studio
3. Sélectionnez l'émulateur

**Option B : Appareil physique**
1. Activez le mode développeur sur votre appareil Android
2. Activez le débogage USB
3. Connectez l'appareil à votre ordinateur
4. Cliquez sur "Run" dans Android Studio
5. Sélectionnez votre appareil

### 3. Première utilisation

1. **Créer un compte** : Utilisez l'écran d'inscription
2. **Se connecter** : Utilisez vos identifiants
3. **Explorer** : Naviguez dans les différentes sections de l'application

## 🏗️ Architecture

L'application suit les **meilleures pratiques recommandées par Google** :

- **Clean Architecture** avec séparation des couches
- **MVVM (Model-View-ViewModel)** pattern
- **Repository Pattern** pour l'abstraction des données
- **Dependency Injection** avec Hilt
- **Jetpack Compose** pour l'UI
- **Navigation Compose** pour la navigation
- **StateFlow/Flow** pour la réactivité
- **DataStore** pour le stockage local (tokens)

### Diagramme d'architecture

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

## 📁 Structure du projet

```
app/src/main/java/com/example/l4m_esports_mobile/
├── data/
│   ├── local/              # DataStore, préférences
│   │   └── PreferencesManager.kt
│   ├── model/               # Modèles de données
│   │   ├── request/        # Requêtes API (Create, Update, etc.)
│   │   └── response/       # Réponses API (UserResponse, GameResponse, etc.)
│   ├── remote/             # Services Retrofit
│   │   ├── ApiService.kt
│   │   ├── AuthApiService.kt
│   │   ├── GameApiService.kt
│   │   ├── TeamApiService.kt
│   │   ├── EventApiService.kt
│   │   ├── MatchApiService.kt
│   │   ├── NotificationApiService.kt
│   │   ├── TwitchApiService.kt
│   │   └── AiApiService.kt
│   └── repository/         # Repositories
│       ├── AuthRepository.kt
│       ├── UserRepository.kt
│       ├── GameRepository.kt
│       ├── TeamRepository.kt
│       ├── EventRepository.kt
│       ├── MatchRepository.kt
│       ├── NotificationRepository.kt
│       ├── TwitchRepository.kt
│       └── AiRepository.kt
├── ui/
│   ├── screens/            # Écrans Compose
│   │   ├── auth/
│   │   │   ├── SignInScreen.kt
│   │   │   └── SignUpScreen.kt
│   │   ├── games/
│   │   │   ├── GamesListScreen.kt
│   │   │   ├── GameDetailScreen.kt
│   │   │   └── CreateGameScreen.kt
│   │   ├── teams/
│   │   │   ├── TeamsListScreen.kt
│   │   │   ├── TeamDetailScreen.kt
│   │   │   ├── CreateTeamScreen.kt
│   │   │   └── EditTeamScreen.kt
│   │   ├── events/
│   │   │   ├── EventDetailScreen.kt
│   │   │   ├── CreateEventScreen.kt
│   │   │   └── EditEventScreen.kt
│   │   ├── matches/
│   │   │   └── MatchDetailScreen.kt
│   │   ├── notifications/
│   │   │   └── NotificationsListScreen.kt
│   │   ├── profile/
│   │   │   ├── ProfileScreen.kt
│   │   │   └── EditProfileScreen.kt
│   │   └── support/
│   │       └── AiHelpWidget.kt
│   ├── components/         # Composants réutilisables
│   │   ├── MainScreen.kt
│   │   └── EventItem.kt
│   └── viewmodel/          # ViewModels
│       ├── AuthViewModel.kt
│       ├── UserViewModel.kt
│       ├── GameViewModel.kt
│       ├── TeamViewModel.kt
│       ├── EventViewModel.kt
│       ├── MatchViewModel.kt
│       ├── NotificationViewModel.kt
│       └── AiViewModel.kt
├── navigation/             # Navigation Compose
│   └── NavGraph.kt
├── di/                     # Injection de dépendances (Hilt)
│   ├── NetworkModule.kt
│   └── AppModule.kt
├── util/                   # Utilitaires
│   ├── Constants.kt
│   ├── Result.kt
│   ├── NetworkErrorHandler.kt
│   ├── LocationService.kt
│   ├── MapUtils.kt
│   └── formatDate.kt
└── MainActivity.kt
```

## ✨ Fonctionnalités

### Authentification
- ✅ Inscription avec validation
- ✅ Connexion avec stockage sécurisé du token JWT
- ✅ Déconnexion
- ✅ Gestion du profil utilisateur

### Gestion des jeux
- ✅ Liste des jeux avec filtres
- ✅ Détails d'un jeu
- ✅ Création/modification/suppression (Admin uniquement)
- ✅ Liste des événements par jeu
- ✅ Filtrage des événements en présentiel

### Gestion des équipes
- ✅ Liste des équipes avec filtres (jeu, statut)
- ✅ Détails d'une équipe
- ✅ Création/modification/suppression d'équipe (avec filtrage des jeux disponibles)
- ✅ Gestion des membres (invitation, retrait)
- ✅ Gestion des demandes d'adhésion
- ✅ Transfert de capitaine
- ✅ Synchronisation en temps réel via Socket.io

### Gestion des événements
- ✅ Liste des événements avec filtres (jeu, statut, à venir)
- ✅ Détails d'un événement avec bracket
- ✅ Affichage du vainqueur final
- ✅ Intégration Google Maps pour événements en présentiel
- ✅ Création/modification d'événement (Admin uniquement)
- ✅ Génération/régénération de bracket (Admin uniquement)
- ✅ Mise à jour automatique du statut (basé sur les dates et les matchs)
- ✅ Synchronisation en temps réel via Socket.io

### Gestion des matchs
- ✅ Liste des matchs avec filtres (événement, équipe, statut)
- ✅ Détails d'un match
- ✅ Affichage du vainqueur pour les matchs terminés
- ✅ Mise à jour du statut/score/validation (Admin uniquement)
- ✅ Transition automatique : in_progress → pending_validation → finished
- ✅ Synchronisation en temps réel via Socket.io

### Notifications
- ✅ Liste des notifications avec filtres
- ✅ Marquer comme lue / toutes comme lues
- ✅ Suppression de notifications
- ✅ Badge avec compteur de non lues
- ✅ Affichage des notifications par priorité

### Intégration Twitch
- ✅ Liaison du compte Twitch au profil
- ✅ Test et aperçu des informations Twitch
- ✅ Affichage des statistiques (followers, vues, statut live)

### Assistant IA
- ✅ Widget flottant d'assistant IA
- ✅ Interface de chat contextuelle
- ✅ Contexte automatique (page actuelle, rôle utilisateur)
- ✅ Gestion des états (chargement, erreur, succès)

### Géolocalisation
- ✅ Demande de permissions de localisation
- ✅ Récupération de la position GPS
- ✅ Filtrage des événements en présentiel
- ✅ Intégration Google Maps pour afficher le lieu

## 📦 Technologies utilisées

### Core Android
- **Kotlin** : Langage de programmation principal
- **Jetpack Compose** : Framework UI déclaratif moderne
- **Material 3** : Design system

### Architecture
- **Hilt** : Injection de dépendances
- **ViewModel** : Gestion de l'état UI
- **StateFlow/Flow** : Programmation réactive
- **Navigation Compose** : Navigation entre écrans

### Networking
- **Retrofit** : Client HTTP type-safe
- **OkHttp** : Client HTTP avec intercepteurs
- **Gson** : Sérialisation/désérialisation JSON

### Asynchrone
- **Coroutines** : Programmation asynchrone
- **Kotlinx Coroutines Play Services** : Intégration avec Google Play Services

### Stockage local
- **DataStore** : Stockage des préférences utilisateur (tokens)

### UI/UX
- **Coil** : Chargement d'images
- **Material Icons Extended** : Icônes Material

### Services Google
- **Google Play Services Location** : Géolocalisation
- **Google Maps** : Intégration pour afficher les lieux

## 🔐 Authentification

L'application utilise JWT (JSON Web Tokens) pour l'authentification :

1. **Sign Up / Sign In** : Le token JWT est reçu du serveur
2. **Stockage** : Le token est stocké dans DataStore de manière sécurisée
3. **Intercepteur** : Le token est automatiquement ajouté aux requêtes via `AuthInterceptor`
4. **Sign Out** : Le token est supprimé lors de la déconnexion

## 🧪 Tests

### Tester l'application

1. **Assurez-vous que le serveur backend est démarré**
   - Voir [l4m-esports-server](../l4m-esports-server/README.md)

2. **Configurez l'URL de l'API**
   - Modifiez `Constants.kt` selon votre configuration

3. **Lancez l'application**
   - Sur un émulateur ou un appareil physique

4. **Testez les fonctionnalités**
   - Créez un compte
   - Explorez les différentes sections
   - Testez les fonctionnalités selon votre rôle (Member, Captain, Admin)

## 📝 Notes importantes

- **Modèles de données** : Tous les modèles correspondent aux modèles du serveur
- **Gestion des erreurs** : Les erreurs réseau sont gérées de manière centralisée via `NetworkErrorHandler`
- **États UI** : Les états sont gérés avec des `sealed class` pour une meilleure type-safety
- **Navigation** : La navigation utilise des routes typées pour éviter les erreurs
- **Permissions** : Les permissions de localisation sont demandées à la demande

## 🐛 Dépannage

### L'application ne se connecte pas au serveur

1. Vérifiez que le serveur backend est en cours d'exécution
2. Vérifiez l'URL dans `Constants.kt`
3. Pour appareil physique, vérifiez que l'IP est correcte
4. Vérifiez que le serveur accepte les connexions depuis votre appareil/émulateur

### Erreur de permissions

- Les permissions de localisation sont demandées à la demande
- Vérifiez que les permissions sont accordées dans les paramètres de l'appareil

### Erreur de build

- Synchronisez le projet avec Gradle : "Sync Project with Gradle Files"
- Nettoyez le projet : "Build > Clean Project"
- Reconstruisez : "Build > Rebuild Project"

## 📚 Ressources

- [Android Developers - Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Android Developers - Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- [Android Developers - Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- [Material 3 Design](https://m3.material.io/)

## 👤 Auteurs

**Équipe L4M Esports**

Ce projet a été développé dans le cadre du cours IFT717 - Projet session.

**Date de création** : 2025

Pour plus d'informations sur le projet, consultez le [Rapport de projet](../RAPPORT_PROJET.md).

