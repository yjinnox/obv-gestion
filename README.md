# OBV Gestion

Gestion de dépôt et de bars/maquis : réceptions fournisseur, stock, ventes, transferts, sessions de caisse et rapports.

La spécification fonctionnelle et technique complète est dans [`docs/specification.md`](docs/specification.md) — c'est le document de référence pour toute règle de gestion (`RG-xx`).

## État du projet

**Phase P0 — squelette** : monorepo Maven, Liquibase, docker-compose, Actuator, CI. Aucune fonctionnalité métier n'est encore implémentée (l'authentification et le référentiel arrivent en P1/P2, cf. §18 de la spécification).

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
- API backend : http://localhost:8080/api/v1
- Actuator : http://localhost:8080/actuator/health
- MailHog (emails de test) : http://localhost:8025

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
