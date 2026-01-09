# Tests - L4M Esports Server

Ce dossier contient les tests automatisés pour le serveur L4M Esports.

## 📋 Prérequis

Avant de lancer les tests, assurez-vous d'avoir :

1. **Redis** en cours d'exécution (via Docker Compose recommandé)
   - MongoDB n'est **pas nécessaire** car les tests utilisent `mongodb-memory-server` (MongoDB en mémoire)
2. Les dépendances installées : `npm install`

## 🚀 Installation des dépendances de test

Les dépendances de test sont déjà dans `package.json`. Si vous venez de cloner le projet :

```bash
npm install
```

## ⚙️ Configuration

Les tests utilisent **MongoDB Memory Server** pour simuler une base de données MongoDB en mémoire. Cela signifie :

- ✅ **Pas besoin d'une instance MongoDB en cours d'exécution**
- ✅ **Tests plus rapides** (pas de connexion réseau)
- ✅ **Isolation complète** (chaque suite de tests a sa propre instance)
- ✅ **Nettoyage automatique** (la base de données est supprimée après les tests)

**Redis** : Les tests utilisent la même instance Redis que le développement (les données sont nettoyées après chaque test). Assurez-vous que Redis est en cours d'exécution via Docker Compose.

## 🧪 Lancer les tests

### Tous les tests

```bash
npm test
```

### Tests en mode watch (re-lance automatiquement lors des changements)

```bash
npm run test:watch
```

### Tests avec couverture de code

```bash
npm run test:coverage
```

Le rapport de couverture sera généré dans le dossier `coverage/`.

## 📁 Structure des tests

```
tests/
├── setup.js          # Configuration et setup/teardown pour les tests
├── auth.test.js      # Tests d'authentification (sign-up, sign-in, sign-out)
└── README.md         # Ce fichier
```

## ✅ Tests d'authentification

Le fichier `auth.test.js` contient des tests complets pour toutes les routes d'authentification :

### POST /api/auth/sign-up
- ✅ Inscription réussie avec tous les champs
- ✅ Inscription réussie avec champs minimaux
- ✅ Inscription sans twitchUsername
- ✅ Validation : email manquant
- ✅ Validation : password manquant
- ✅ Validation : firstName manquant
- ✅ Validation : lastName manquant
- ✅ Validation : firstName vide
- ✅ Validation : lastName vide
- ✅ Validation : format email invalide
- ✅ Validation : password trop court
- ✅ Erreur : email déjà existant
- ✅ Erreur : twitchUsername déjà pris

### POST /api/auth/sign-in
- ✅ Connexion réussie avec identifiants valides
- ✅ Connexion avec email en différentes casse
- ✅ Validation : email manquant
- ✅ Validation : password manquant
- ✅ Erreur : email inexistant
- ✅ Erreur : password incorrect
- ✅ Gestion des sessions : révocation de l'ancien token lors d'une nouvelle connexion

### POST /api/auth/sign-out
- ✅ Déconnexion réussie
- ✅ Révocation du token après déconnexion
- ✅ Erreur : pas de token fourni
- ✅ Erreur : token invalide
- ✅ Erreur : token révoqué

### Flux complet
- ✅ Inscription → Connexion → Déconnexion → Réutilisation du token (échec)

## 🔧 Dépannage

### Erreur : "Redis client not initialized"

Si vous obtenez cette erreur, assurez-vous que Redis est en cours d'exécution :

```bash
docker-compose up -d
```

### Erreur : "MongoDB connection error"

Les tests utilisent `mongodb-memory-server`, donc cette erreur ne devrait normalement pas se produire. Si c'est le cas :

1. Vérifiez que `mongodb-memory-server` est installé : `npm install`
2. Vérifiez que vous avez suffisamment d'espace disque (mongodb-memory-server télécharge MongoDB en arrière-plan)
3. Sur certains systèmes, il peut y avoir des problèmes de permissions - vérifiez les logs

### Les tests échouent de manière inattendue

1. Assurez-vous que Redis est accessible et en cours d'exécution
2. Vérifiez que les variables d'environnement sont correctement configurées
3. MongoDB Memory Server se télécharge automatiquement au premier lancement - assurez-vous d'avoir une connexion internet

## 📝 Ajouter de nouveaux tests

Pour ajouter de nouveaux tests :

1. Créez un nouveau fichier `*.test.js` dans le dossier `tests/`
2. Importez les dépendances nécessaires (app, models, services, etc.)
3. Utilisez `setupTests()`, `cleanupTests()`, et `cleanupAfterEach()` pour gérer la base de données
4. Suivez la structure des tests existants pour la cohérence

Exemple :

```javascript
import request from 'supertest';
import app from '../app.js';
import { setupTests, cleanupTests, cleanupAfterEach } from './setup.js';

describe('My Feature Tests', () => {
  beforeAll(async () => {
    await setupTests();
  });

  afterAll(async () => {
    await cleanupTests();
  });

  afterEach(async () => {
    await cleanupAfterEach();
  });

  test('Should do something', async () => {
    const response = await request(app)
      .get('/api/my-route')
      .expect(200);

    expect(response.body.success).toBe(true);
  });
});
```

## 🎯 Bonnes pratiques

- **Isolation** : Chaque test doit être indépendant et ne pas dépendre d'autres tests
- **Nettoyage** : Utilisez `cleanupAfterEach()` pour nettoyer les données après chaque test
- **Nommage** : Utilisez des noms descriptifs pour les tests (ex: "Should return 400 if email is missing")
- **Assertions** : Testez à la fois les cas de succès et d'échec
- **Couverture** : Viser une couverture de code élevée (> 80%)

