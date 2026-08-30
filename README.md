# OBV Gestion

Gestion de dépôt et de bars/maquis : réceptions fournisseur, stock, ventes, transferts, sessions de caisse et rapports.

La spécification fonctionnelle et technique complète est dans [`docs/specification.md`](docs/specification.md) — c'est le document de référence pour toute règle de gestion (`RG-xx`).

## État du projet

- **P0 — squelette** : monorepo Maven, Liquibase, docker-compose, Actuator, CI.
- **P1 — authentification** : cycle de vie des comptes (création, invitation, mot de passe, OTP, activation, désactivation/réactivation, archivage RG-05), rôles et permissions (§3.3), JWT + refresh token rotatif, verrouillage après échecs, notifications email/SMS par file transactionnelle (outbox), amorçage du premier SUPER_ADMINISTRATEUR. Le référentiel (marques, produits, tarifs...) arrive en P2, cf. §18 de la spécification.

## Stack

- **Backend** : Java 25, Spring Boot 4.1, PostgreSQL 17, Redis 7, Liquibase, Maven multi-module.
- **Frontend** : Angular 22 (composants standalone, signals, zoneless), Angular Material.
- **Infra** : Docker / Docker Compose, GitHub Actions.

## Structure

```
obv-gestion/
├── obv-domain/          entités JPA, enums, règles métier pures, VO
├── obv-application/     services, cas d'usage, ports (interfaces)
├── obv-infrastructure/  repositories, adaptateurs SMS/mail/PDF, config Redis
├── obv-api/             contrôleurs REST, DTO, mappers, gestion d'erreurs
├── obv-bootstrap/       application Spring Boot, config, changelogs Liquibase
├── frontend/             application Angular
└── docs/specification.md
```

## Démarrer en local

```bash
docker compose up --build
```

- Frontend : http://localhost:4200
- API backend : http://localhost:8080/api/v1 (documentation OpenAPI : `/swagger-ui.html`)
- Actuator : http://localhost:8080/actuator/health
- MailHog (emails de test) : http://localhost:8025

Le `docker-compose.yml` amorce automatiquement un premier `SUPER_ADMINISTRATEUR`
(`admin@obv-gestion.local`) au démarrage (RG-06 : il en faut toujours au moins
un) : son invitation d'activation part par email vers MailHog. Les variables
`JWT_SECRET`, `BOOTSTRAP_ADMIN_*`, `NOTIFICATION_MODE` et `SMS_PROVIDER` y
sont définies avec des valeurs de développement — à ne jamais réutiliser en
production (secrets réels via variables d'environnement, cf. §16.2).

## Développement backend

```bash
mvn -q verify
```

Nécessite un JDK 25. Les tests d'intégration utilisent Testcontainers (PostgreSQL + Redis réels, aucun test sur H2) : un démon Docker est requis pour les exécuter.

## Développement frontend

```bash
cd frontend
npm ci
npm start   # serveur de dev
npm run build
npm test    # tests unitaires Vitest
```

Nécessite Node ≥ 22.22.3.
