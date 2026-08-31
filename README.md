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
- Actuator (santé, métriques Prometheus, traces) : http://localhost:8081/actuator/health — port distinct de l'API publique (§16.2)
- MailHog (emails de test) : http://localhost:8025

Le `docker-compose.yml` amorce automatiquement un premier `SUPER_ADMINISTRATEUR`
(`admin@obv-gestion.local`) au démarrage (RG-06 : il en faut toujours au moins
un) : son invitation d'activation part par email vers MailHog. Les variables
`JWT_SECRET`, `BOOTSTRAP_ADMIN_*`, `NOTIFICATION_MODE` et `SMS_PROVIDER` y
sont définies avec des valeurs de développement — à ne jamais réutiliser en
production (secrets réels via variables d'environnement, cf. §16.2).

## Déployer pour des testeurs (VM gratuite)

`docker-compose.prod.yml` reprend la même stack, avec le profil Spring `prod`
(§16.2 : Swagger et le détail d'Actuator désactivés) et un vrai envoi
d'email (ex. [Brevo](https://www.brevo.com), gratuit jusqu'à 300 emails/jour)
à la place de MailHog. Seul le frontend est exposé publiquement ; il
reverse-proxy `/api/` vers le backend sur le réseau Docker interne
(`frontend/nginx.conf`) — Postgres, Redis et le backend ne sont jamais
accessibles depuis l'extérieur.

Sur une VM gratuite (ex. [Oracle Cloud Always Free](https://www.oracle.com/cloud/free/)) avec Docker installé :

```bash
git clone <url-du-repo> && cd obv-gestion
cp .env.example .env
# éditer .env : mot de passe DB, JWT_SECRET, identifiants SMTP Brevo,
# APP_FRONTEND_URL = IP publique (ou domaine) de la VM, email admin
docker compose -f docker-compose.prod.yml up --build -d
```

Le premier `SUPER_ADMINISTRATEUR` (adresse `BOOTSTRAP_ADMIN_EMAIL`) reçoit
alors une vraie invitation d'activation par email (§4.2), et peut ensuite
créer les comptes testeurs depuis l'écran "Utilisateurs".

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

## Déploiement Kubernetes

Chart Helm sous [`k8s/obv-gestion`](k8s/obv-gestion) (§16.2) : `Deployment`/
`Service`/`HorizontalPodAutoscaler`/`PodDisruptionBudget` pour le backend,
`Deployment`/`Service` pour le frontend, `Ingress` (TLS), `ConfigMap` et
`Secret`. PostgreSQL et Redis sont des dépendances externes, non déployées
par ce chart.

```bash
helm lint k8s/obv-gestion
helm install obv-gestion k8s/obv-gestion \
  --namespace obv-gestion --create-namespace \
  --set backend.image.repository=ghcr.io/<owner>/<repo>/backend \
  --set frontend.image.repository=ghcr.io/<owner>/<repo>/frontend \
  --set backend.image.tag=<sha-ou-tag> \
  --set frontend.image.tag=<sha-ou-tag> \
  -f mes-valeurs-secretes.yaml   # jamais de secret en clair committé (§14.3)
```

Voir les commentaires de [`values.yaml`](k8s/obv-gestion/values.yaml) pour
le détail de chaque option (dont `secrets.existingSecretName` si les
identifiants sont gérés hors chart, par ex. via un opérateur de secrets).
