# Collection Postman - L4M Esports API

Cette collection Postman permet de tester toutes les routes implémentées de l'API L4M Esports.

## Installation

1. Ouvrir Postman
2. Cliquer sur **Import**
3. Importer les fichiers :
   - `L4M_Esports_API.postman_collection.json` (Collection)
   - `L4M_Esports_Local.postman_environment.json` (Environnement)

## Configuration

### Variables d'environnement

L'environnement "L4M Esports - Local" contient :
- `base_url` : `http://localhost:3000` (URL du serveur)
- `token` : Token JWT (sera automatiquement rempli après sign-in)
- `user_id` : ID de l'utilisateur (pour les tests)

### Utilisation

1. **Sélectionner l'environnement** : "L4M Esports - Local" dans le menu déroulant en haut à droite

2. **Tester l'authentification** :
   - Exécuter `Auth > Sign Up` pour créer un compte
   - Copier le `token` de la réponse
   - Coller le token dans la variable `token` de l'environnement
   - Exécuter `Auth > Sign In` pour se connecter
   - Le token sera automatiquement utilisé dans les autres requêtes

## Routes disponibles

### Auth
- ✅ `POST /api/auth/sign-up` - Inscription
- ✅ `POST /api/auth/sign-in` - Connexion
- ✅ `POST /api/auth/sign-out` - Déconnexion

### Users
- ✅ `GET /api/users` - Liste tous les utilisateurs (Admin)
- ✅ `GET /api/users/me` - Profil de l'utilisateur connecté
- ✅ `PUT /api/users/me` - Mettre à jour son profil
- ✅ `GET /api/users/:id` - Profil d'un utilisateur spécifique
- ✅ `PUT /api/users/:id` - Mettre à jour un utilisateur (Admin)
- ✅ `PUT /api/users/:id/role` - Changer le rôle d'un utilisateur (Admin)
- ✅ `DELETE /api/users/:id` - Supprimer un utilisateur (Admin)

### Health
- ✅ `GET /health` - Health check
- ✅ `GET /` - Message d'accueil

## Notes

- Pour les routes protégées, le token doit être défini dans la variable `token`
- Pour les routes Admin, vous devez être connecté avec un compte admin
- Les routes avec `:id` nécessitent de remplacer `:id` par un ID utilisateur valide

