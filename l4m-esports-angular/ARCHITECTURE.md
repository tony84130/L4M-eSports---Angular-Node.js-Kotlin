# L4M Esports Web (Angular) — Architecture

## Objectif
Scaffolding minimal pour lancer le front web en local, aligné sur l’architecture décidée : feature-first, composants standalone, services + signals, lib API dédiée, prêt pour temps réel et PWA/offline.

## Arborescence créée
```
l4m-esports-angular/
├── src/
│   ├── app/
│   │   ├── core/                 # Services transverses (auth, session, ws), guards, interceptors, layout global
│   │   ├── shared/               # UI réutilisable, design tokens, composants agnostiques
│   │   ├── api/                  # Client REST/WS centralisé, DTO/mapper backend -> UI
│   │   ├── features/             # Découpage métier (lazy)
│   │   │   ├── auth/
│   │   │   ├── users/
│   │   │   ├── teams/
│   │   │   ├── events/
│   │   │   ├── matches/
│   │   │   └── notifications/    # Centre de notif, bridge WebSocket, hook push PWA
│   │   └── app.config.ts         # Routing principal, providers (standalone)
│   └── assets/                   # Logos, manifest, service worker
└── package.json                  # (à générer via Angular CLI)
```

## Principes clés
- **Feature-first + standalone** : chaque feature encapsule UI + services + routing, lazy par défaut.
- **État** : services + signals par feature ; store global (NgRx/SignalStore) seulement pour flux complexes (live scores/offline).
- **Temps réel** : service WebSocket central en `core` qui diffuse vers les features pour éviter les sockets multiples.
- **Offline/PWA** : manifest + service worker ; cache HTTP de base et persistence locale (IndexedDB/localforage) pour les entités critiques si nécessaire.
- **API dédiée** : la lib `api/` encapsule les routes backend (auth, users, teams, events, matches, notifications) et fournit des DTO typés.

## Prochaines étapes (sans code ici)
- Initialiser le projet : `ng new l4m-esports-angular --standalone --routing --style=scss`.
- Configurer `app.config.ts` avec lazy routes par feature.
- Ajouter le service WS central dans `core` et les interceptors (auth, error).
- Mettre en place la lib `api/` (types, clients REST, bridge WS).
- Activer PWA : `ng add @angular/pwa` (manifest + service worker).

