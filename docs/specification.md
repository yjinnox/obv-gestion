# OBV Gestion — Spécification fonctionnelle et technique

Document destiné à Claude Code. Version 1.0.

---

## 0. Instructions à Claude Code

- Implémenter **uniquement** ce qui est décrit ici. Toute règle absente doit être posée en question, pas inventée.
- Respecter l'ordre des phases (§16). Chaque phase doit compiler, passer ses tests et démarrer via `docker compose up` avant de passer à la suivante.
- Toute règle de gestion est identifiée `RG-xx` : la référencer en commentaire Javadoc sur la méthode qui l'implémente.
- Interdits : `spring.jpa.hibernate.ddl-auto` autre que `validate`, `DELETE` physique sur données transactionnelles, calcul monétaire en `double`/`float`, secrets en dur.
- Code et commentaires en français pour le métier (noms d'entités, DTO), anglais pour la technique (couches, infra).
- Privilégier les solutions simples et lisibles : pas d'abstraction spéculative, pas de framework maison.

---

## 1. Glossaire métier

| Terme | Définition |
|---|---|
| **Dépôt** | Point de vente qui vend **exclusivement** au casier ou demi-casier. Approvisionné par des fournisseurs. |
| **Bar / Maquis** | Point de vente qui vend **exclusivement** à la bouteille au consommateur final. Approvisionné par le dépôt. |
| **Marque** | Marque de boisson (ex. Flag, Coca-Cola, Awa). |
| **Produit** | Couple Marque + Volume (ex. « Flag 33 cl »). Unité de référence du catalogue. |
| **Conditionnement** | Casier d'un produit donné, caractérisé par sa capacité en bouteilles (12, 16, 24…). |
| **Demi-casier** | Moitié d'un casier. Autorisé uniquement si la capacité du casier est paire. |
| **Réception** | Enregistrement d'une livraison fournisseur au dépôt. |
| **Transfert** | Mouvement interne dépôt → bar : sortie en casiers, entrée en bouteilles. |
| **Session de vente** | Journée de caisse d'un point de vente : ouverture → ventes → clôture → validation. |
| **Ticket serveur** | Ligne de vente ouverte par le gérant du bar au nom d'un serveur. |

---

## 2. Hypothèses retenues (à confirmer avant développement)

Ces points étaient absents ou ambigus dans l'expression de besoin initiale. Les valeurs ci-dessous sont **des décisions par défaut** ; les modifier si nécessaire.

| # | Hypothèse retenue |
|---|---|
| H1 | Devise : **XOF (FCFA)**, montants entiers, aucune décimale. Stockage `BIGINT` (pas de centimes). |
| H2 | Fuseau horaire métier : `Africa/Abidjan`. Stockage UTC (`TIMESTAMPTZ`), affichage local. |
| H3 | Langue de l'IHM : **français uniquement** (structure i18n en place, un seul bundle). |
| H4 | Pas de TVA ni de mentions fiscales sur les factures en v1. La facture est un document commercial. |
| H5 | Une seule organisation. Modèle multi-**points de vente** (1 dépôt + N bars) dès la v1. |
| H6 | **Pas de gestion de consigne** (bouteilles/casiers vides) en v1. À prévoir en v2 — le modèle ne doit pas l'interdire. |
| H7 | Paiement **comptant uniquement** (espèces / mobile money enregistré comme mode). Pas de crédit client ni d'échéancier en v1. |
| H8 | Pas de mode hors-ligne. L'application exige une connexion. |
| H9 | Prix d'achat fournisseur et prix de vente sont distincts et gérés séparément. |
| H10 | Un utilisateur est rattaché à **un ou plusieurs** points de vente ; ses droits s'appliquent par point de vente. |

---

## 3. Acteurs, rôles et permissions

### 3.1 Rôles

| Rôle | Portée |
|---|---|
| `SUPER_ADMINISTRATEUR` | Global. Seul habilité aux validations définitives et aux modifications post-clôture. |
| `ADMINISTRATEUR` | Global. Gère le référentiel et les utilisateurs, sans droit de validation définitive. |
| `GERANT_DEPOT` | Un point de vente de type DEPOT. |
| `GERANT_BAR` | Un point de vente de type BAR. |
| `VENDEUR` | Consultation + saisie de ventes sur son point de vente. |
| `SERVEUR` | Bar uniquement. N'a pas de compte applicatif en v1 : c'est une **entité référentielle** (`Serveur`) sélectionnée par le gérant. |

### 3.2 Permissions (granulaires, cumulables via les rôles)

```
REFERENTIEL_READ, REFERENTIEL_WRITE
UTILISATEUR_READ, UTILISATEUR_WRITE
RECEPTION_READ, RECEPTION_WRITE, RECEPTION_VALIDER
VENTE_READ, VENTE_WRITE
SESSION_CLOTURER, SESSION_VALIDER
TRANSFERT_WRITE, TRANSFERT_VALIDER
CLIENT_READ, CLIENT_WRITE
RAPPORT_READ
MODIFICATION_POST_CLOTURE
```

### 3.3 Matrice

| Permission | SUPER_ADMIN | ADMIN | GERANT_DEPOT | GERANT_BAR | VENDEUR |
|---|:--:|:--:|:--:|:--:|:--:|
| REFERENTIEL_READ | ✔ | ✔ | ✔ | ✔ | ✔ |
| REFERENTIEL_WRITE | ✔ | ✔ | — | — | — |
| UTILISATEUR_WRITE | ✔ | ✔ | — | — | — |
| RECEPTION_WRITE | ✔ | — | ✔ | — | — |
| RECEPTION_VALIDER | ✔ | — | — | — | — |
| VENTE_WRITE | ✔ | — | ✔ | ✔ | ✔ |
| SESSION_CLOTURER | ✔ | — | ✔ | ✔ | — |
| SESSION_VALIDER | ✔ | — | — | — | — |
| TRANSFERT_WRITE | ✔ | — | ✔ | ✔ | — |
| MODIFICATION_POST_CLOTURE | ✔ | — | — | — | — |
| RAPPORT_READ | ✔ | ✔ | ✔ | ✔ | — |

**RG-01** — La validation définitive d'une réception, d'un transfert ou d'une session de vente est réservée au `SUPER_ADMINISTRATEUR`. Un utilisateur ne peut jamais valider un document qu'il a lui-même clôturé (séparation des tâches), même s'il est SUPER_ADMIN — dans ce cas un autre SUPER_ADMIN doit valider.

---

## 4. Cycle de vie des comptes utilisateurs

### 4.1 Création (par ADMIN ou SUPER_ADMIN)

Champs : nom, prénoms, canal de contact (`EMAIL` ou `TELEPHONE`), email **ou** téléphone (format E.164, ex. `+2250700000000`), rôle(s), point(s) de vente rattaché(s).

### 4.2 Activation

1. Le système crée le compte au statut `EN_ATTENTE_ACTIVATION` et génère un **jeton d'invitation** (UUID opaque, TTL 72 h, usage unique, hashé en base).
2. Envoi du lien par SMS ou email : `https://<host>/activation?token=<uuid>`.
3. L'utilisateur saisit mot de passe + confirmation.
4. Le système envoie un **OTP** au même canal. L'utilisateur le saisit → statut `ACTIF`.

**RG-02** — Politique de mot de passe : ≥ 10 caractères, au moins 1 minuscule, 1 majuscule, 1 chiffre. Hachage **Argon2id** (fallback BCrypt cost 12).

**RG-03** — OTP : **6 chiffres** (correction : 4 chiffres offre 10 000 combinaisons, insuffisant). TTL 10 minutes, stocké haché en Redis, **5 tentatives max** puis invalidation, **3 renvois max par heure**, rate limiting par numéro/IP.

**RG-04** — Un lien ou un OTP consommé, expiré ou invalide renvoie toujours le même message générique (pas d'énumération de comptes).

### 4.3 Administration

- Liste paginée + filtres (nom, rôle, statut, point de vente).
- Actions : activer, désactiver, réinitialiser le mot de passe, modifier rôles/droits, **archiver**.

**RG-05** — La « suppression » d'un utilisateur est une **désactivation + archivage** (`statut = ARCHIVE`, `dateArchivage`). Aucun `DELETE` physique : les documents historiques référencent l'auteur. Un compte archivé ne peut plus se connecter et n'apparaît plus dans les listes de sélection.

**RG-06** — Un utilisateur ne peut ni s'auto-désactiver, ni se retirer son propre rôle. Il doit toujours rester au moins un `SUPER_ADMINISTRATEUR` actif.

### 4.4 Authentification

- Identifiant = email **ou** téléphone + mot de passe.
- JWT access token (15 min) + refresh token rotatif (7 j) stocké en Redis, révocable.
- Verrouillage temporaire du compte après 5 échecs (15 min).

---

## 5. Référentiel

### 5.1 Entités

- **Marque** : `id`, `libelle` (unique), `actif`.
- **Volume** : `id`, `libelle` (« 33 cl »), `contenanceMl` (330), `actif`.
- **Produit** : `id`, `marque`, `volume`, `actif`. Unicité `(marque, volume)`.
- **Conditionnement** : `id`, `produit`, `capaciteBouteilles` (12/16/24…), `demiCasierAutorise` (calculé : `capacite % 2 == 0`), `actif`.
- **Fournisseur** : `id`, `raisonSociale`, `telephone`, `email`, `adresse`, `actif`. *(Absent de la spec initiale — nécessaire pour tracer les réceptions.)*
- **PointDeVente** : `id`, `libelle`, `type` (`DEPOT` | `BAR`), `adresse`, `actif`.
- **Serveur** : `id`, `pointDeVente`, `nom`, `prenoms`, `telephone`, `actif`.
- **Client** : `id`, `type` (`PARTICULIER` | `ENTREPRISE`), `nom`, `prenoms`, `raisonSociale`, `telephone` (unique), `email`, `adresseFacturation`, `actif`.

**RG-07** — `raisonSociale` est obligatoire si `type = ENTREPRISE`, interdite sinon. `nom`/`prenoms` obligatoires si `PARTICULIER`.

### 5.2 Tarification

**Correction majeure** : le prix ne peut pas être un simple attribut du casier. Une modification de prix ne doit **jamais** altérer les documents passés.

- **Tarif** : `id`, `pointDeVente`, `produit`, `uniteVente` (`CASIER` | `BOUTEILLE`), `nature` (`ACHAT` | `VENTE`), `montant`, `dateDebut`, `dateFin` (nullable).

**RG-08** — Un seul tarif actif par `(pointDeVente, produit, uniteVente, nature)` à un instant T. Modifier un prix = clore le tarif courant (`dateFin`) et en créer un nouveau.

**RG-09** — Toute ligne de document (réception, vente, transfert) **fige** le prix unitaire au moment de sa création. Les recalculs ultérieurs utilisent la valeur figée.

**RG-10** — Un point de vente de type `DEPOT` n'utilise que `uniteVente = CASIER`. Un `BAR` n'utilise que `BOUTEILLE`.

---

## 6. Gestion du stock

### 6.1 Unité de stockage

**Correction** : ne pas stocker les quantités en décimal (`0.5 casier`). Risque d'erreurs d'arrondi et de comparaisons.

**RG-11** — Au dépôt, les quantités sont stockées en **demi-casiers entiers** : `quantiteDemiCasiers` (`1` = demi-casier, `2` = un casier). L'IHM saisit et affiche des casiers (pas de 0,5), le mapping se fait dans la couche applicative.

**RG-12** — Au bar, les quantités sont stockées en **bouteilles entières**.

**RG-13** — Le demi-casier n'est proposé que si `conditionnement.demiCasierAutorise = true`.

### 6.2 Modèle

- **Stock** : `id`, `pointDeVente`, `produit`, `quantite` (demi-casiers ou bouteilles selon le type de PDV), `version` (verrou optimiste JPA). Unicité `(pointDeVente, produit)`.
- **MouvementStock** (journal append-only, jamais modifié) : `id`, `pointDeVente`, `produit`, `type`, `quantiteSignee`, `stockAvant`, `stockApres`, `documentType`, `documentId`, `dateHeure`, `utilisateur`.

Types de mouvement : `ENTREE_RECEPTION`, `SORTIE_VENTE`, `SORTIE_TRANSFERT`, `ENTREE_TRANSFERT`, `AJUSTEMENT`, `CONTRE_PASSATION`.

**RG-14** — Toute variation de stock crée un `MouvementStock` dans la **même transaction** que la mise à jour de `Stock`. Le solde de `Stock` doit toujours être égal à la somme des mouvements (invariant vérifié par un test d'intégration).

**RG-15** — Le stock ne peut jamais devenir négatif. Toute opération qui l'entraînerait est rejetée (`409 Conflict`, code `STOCK_INSUFFISANT`) avec le détail du produit et de la quantité disponible.

**RG-16** — La décrémentation du stock utilise le verrou optimiste ; en cas de `OptimisticLockException`, l'opération est rejouée automatiquement (3 tentatives) avant erreur.

---

## 7. Réception au dépôt

### 7.1 États

```
BROUILLON ──clôturer──> EN_ATTENTE_VALIDATION ──valider──> VALIDEE (final)
    ▲                            │
    │                            ├──annuler──> ANNULEE (final)
    └────demande de correction───┘
```

### 7.2 Déroulé

1. Le gérant (`RECEPTION_WRITE`) crée une réception : fournisseur, date/heure de livraison, point de vente.
2. Il ajoute des lignes : produit (marque + volume), conditionnement, nombre de casiers, prix d'achat du casier (pré-rempli depuis le tarif `ACHAT` en vigueur, modifiable).
3. Il clôture → écran récapitulatif : total par marque, par volume, montant total.
4. Il sélectionne un `SUPER_ADMINISTRATEUR` dans la liste et clique « Envoyer pour validation ».
5. Notification (SMS/email) avec lien vers le récapitulatif.
6. Le SUPER_ADMIN s'authentifie et arrive sur le récapitulatif. Trois actions : **Annuler**, **Modifier**, **Valider définitivement**.

### 7.3 Règles

**RG-17** — Le stock est incrémenté **dès la clôture** par le gérant (`ENTREE_RECEPTION`), afin que la marchandise soit vendable immédiatement. *(Décision explicite : l'attente de la validation du SUPER_ADMIN bloquerait l'exploitation.)*

**RG-18** — « Annuler » ne supprime **rien** en base. La réception passe à `ANNULEE`, des mouvements `CONTRE_PASSATION` de signe opposé sont générés, un motif d'annulation est obligatoire, et l'auteur/horodatage sont tracés. *(Correction de la demande initiale « effacer de la base de données » : incompatible avec l'auditabilité et l'intégrité référentielle.)*

**RG-19** — L'annulation est refusée si tout ou partie de la marchandise reçue a déjà été vendue ou transférée (contrôle sur le stock disponible). Message explicite indiquant les produits en cause.

**RG-20** — « Modifier » : le SUPER_ADMIN peut changer produit, quantité, prix unitaire, ou supprimer/ajouter une ligne. Chaque modification génère un mouvement d'ajustement et une entrée d'audit (valeur avant / après).

**RG-21** — Une réception `VALIDEE` est **immuable**. Toute correction ultérieure passe par un document d'ajustement distinct.

**RG-22** — La confirmation d'annulation affiche une modale explicite (« action irréversible »), avec saisie obligatoire du motif. « Non » revient au récapitulatif sans effet.

---

## 8. Vente au dépôt

### 8.1 Session de vente

**Ajout** : la spec initiale parle de « journée de vente » sans en définir l'objet. Il est nécessaire.

- **SessionVente** : `id`, `pointDeVente`, `dateOuverture`, `ouvertePar`, `fondCaisse`, `statut`, `dateCloture`, `clotureePar`, `totalTheorique`, `totalCompte`, `ecart`, `dateValidation`, `valideePar`.
- États : `OUVERTE → CLOTUREE → (VALIDEE | EN_MODIFICATION → VALIDEE)`.

**RG-23** — Une seule session `OUVERTE` par point de vente à la fois. Aucune vente ne peut être créée hors session ouverte.

### 8.2 Panier et commande

1. Le gérant sélectionne : marque, volume, nombre de casiers (pas de 0,5 si autorisé).
2. « Ajouter au panier ». Le panier est persisté (Redis, TTL 4 h, clé = utilisateur + session).
3. Écran panier : lignes (produit, volume, nb casiers, PU, total ligne) + montant global.
4. Actions : **supprimer le panier**, **modifier le panier** (quantité, retrait de ligne), **valider**.
5. À la validation : sélection du client dans la liste, ou création à la volée.
6. « Commander » → génération du **bon de commande** (pour les manutentionnaires) et de la **facture** (pour le client), en PDF.

**RG-24** — Le stock est décrémenté au moment de « Commander », pas à l'ajout au panier. Un contrôle de disponibilité est fait à l'ajout (informatif) **et** à la commande (bloquant).

**RG-25** — La suppression du panier n'a aucun effet sur le stock ni sur la base (le panier n'est pas un document).

**RG-26** — Numérotation : `BC-{PDV}-{AAAA}-{séquence}` et `FA-{PDV}-{AAAA}-{séquence}`. Séquences PostgreSQL dédiées par point de vente et par année, jamais réutilisées, sans trou toléré (utiliser une table de compteur avec verrou pessimiste si la continuité est exigée).

**RG-27** — Idempotence : l'endpoint de commande accepte un en-tête `Idempotency-Key`. Un double clic ne crée pas deux commandes.

### 8.3 Clôture

À la clôture, l'application affiche :
- stock restant par produit,
- quantités vendues par marque, par volume et global,
- montant de la recette (par mode de paiement),
- écart de caisse si `totalCompte` est saisi.

Deux actions : **Valider la vente** ou **Demander une modification**.

**RG-28** — Une session `VALIDEE` est définitivement figée : plus aucune ligne modifiable, par personne.

**RG-29** — « Demander une modification » notifie le SUPER_ADMIN (SMS/email + lien). Statut `EN_MODIFICATION`. Seul le SUPER_ADMIN peut alors modifier les quantités vendues ; chaque modification génère un mouvement d'ajustement de stock et une entrée d'audit. Il clôture ensuite par une validation définitive.

---

## 9. Transfert dépôt → bar

**Ajout majeur** : la spec initiale indique que « le maquis passe commande auprès du dépôt » sans décrire le flux. Sans lui, les deux stocks sont incohérents.

- **BonTransfert** : `id`, `numero`, `pdvSource` (DEPOT), `pdvDestination` (BAR), `date`, `statut`, lignes.
- **LigneTransfert** : `produit`, `quantiteDemiCasiers` (sortie dépôt), `quantiteBouteilles` (entrée bar), `prixCessionUnitaire`.

**RG-30** — `quantiteBouteilles = (quantiteDemiCasiers × capaciteBouteilles) / 2`. Le transfert est refusé si le résultat n'est pas entier.

**RG-31** — Le transfert génère deux mouvements atomiques dans la même transaction : `SORTIE_TRANSFERT` au dépôt, `ENTREE_TRANSFERT` au bar.

**RG-32** — Workflow identique à la réception : `BROUILLON → EN_ATTENTE_VALIDATION → VALIDEE | ANNULEE`, validation SUPER_ADMIN.

---

## 10. Vente au bar / maquis

Fonctionnement identique au dépôt, avec ces différences :

- Unité de vente : **bouteille**.
- Le gérant ouvre une **ligne de vente par serveur** (`TicketServeur` : `id`, `session`, `serveur`, `lignes`, `montantTotal`, `statut`, `dateEncaissement`).
- Le serveur commande auprès du gérant, le gérant encaisse, valide, remet les bouteilles.
- Pas de facture client ni de sélection de client (vente au comptoir). Ticket imprimable.

**RG-33** — Un serveur peut avoir plusieurs tickets dans la journée. Le récapitulatif de clôture détaille : total par serveur, total par marque/volume, total général.

**RG-34** — La clôture du bar suit le même schéma que le dépôt (validation définitive ou demande de modification au SUPER_ADMIN).

---

## 11. Notifications

- **Canaux** : SMS et email. Interface `NotificationSender` avec deux implémentations et une implémentation `Log` pour le développement.
- **Fournisseur SMS** : abstraction derrière un port. Configurer un adaptateur par variable d'environnement (`sms.provider=twilio|orange|log`). Ne pas coder en dur un fournisseur.
- **Email** : SMTP standard via `spring-boot-starter-mail`.
- **Templates** : Thymeleaf, stockés en ressources, avec variables typées.
- **Envoi asynchrone** : file en base (`notification_outbox`) + job de relance (pattern transactional outbox), 3 tentatives avec backoff. Un échec d'envoi ne doit jamais faire échouer la transaction métier.

**RG-35** — Les liens envoyés ne portent **aucun droit** : ils redirigent vers une page qui exige l'authentification. Le jeton identifie le document, pas l'utilisateur.

**RG-36** — Un jeton de validation est lié au destinataire sélectionné, à usage unique, TTL 72 h.

---

## 12. Audit et traçabilité

- Toutes les entités persistantes : `createdAt`, `createdBy`, `updatedAt`, `updatedBy` (`@CreatedDate`, `@CreatedBy`, JPA Auditing).
- **Hibernate Envers** activé sur : `Reception`, `LigneReception`, `Vente`, `LigneVente`, `SessionVente`, `Tarif`, `Utilisateur`, `Stock`.
- Table `journal_action` pour les actions sensibles : validation, annulation, modification post-clôture, changement de droits, activation/désactivation de compte. Champs : acteur, action, cible, valeurs avant/après (JSONB), IP, horodatage.

---

## 13. API REST

Base : `/api/v1`. Authentification `Bearer`. Erreurs au format **RFC 7807** (`ProblemDetail`), avec un `code` métier stable.

```
POST   /auth/login
POST   /auth/refresh
POST   /auth/logout
POST   /auth/activation/{token}/mot-de-passe
POST   /auth/activation/{token}/otp
POST   /auth/otp/renvoyer

GET    /utilisateurs                      ?page&size&statut&role&pdv
POST   /utilisateurs
PATCH  /utilisateurs/{id}
POST   /utilisateurs/{id}/activer
POST   /utilisateurs/{id}/desactiver
POST   /utilisateurs/{id}/archiver

CRUD   /marques /volumes /produits /conditionnements /fournisseurs /clients /serveurs /points-de-vente
GET    /tarifs                            ?pdv&produit&nature
POST   /tarifs                            (clôt automatiquement le tarif courant)

POST   /receptions
POST   /receptions/{id}/lignes
PATCH  /receptions/{id}/lignes/{ligneId}
DELETE /receptions/{id}/lignes/{ligneId}   (brouillon uniquement)
POST   /receptions/{id}/cloturer
POST   /receptions/{id}/demander-validation   { destinataireId }
GET    /receptions/{id}/recapitulatif
POST   /receptions/{id}/valider
POST   /receptions/{id}/annuler               { motif }

POST   /sessions-vente/ouvrir
GET    /sessions-vente/courante?pdv={id}
POST   /sessions-vente/{id}/cloturer          { totalCompte }
POST   /sessions-vente/{id}/valider
POST   /sessions-vente/{id}/demander-modification
GET    /sessions-vente/{id}/recapitulatif

GET    /panier
POST   /panier/lignes
PATCH  /panier/lignes/{id}
DELETE /panier/lignes/{id}
DELETE /panier
POST   /commandes                             (Idempotency-Key requis)
GET    /commandes/{id}/bon-de-commande.pdf
GET    /commandes/{id}/facture.pdf

POST   /tickets-serveur
POST   /tickets-serveur/{id}/lignes
POST   /tickets-serveur/{id}/encaisser

POST   /transferts
POST   /transferts/{id}/cloturer
POST   /transferts/{id}/valider
POST   /transferts/{id}/annuler

GET    /stocks?pdv={id}
GET    /mouvements-stock?pdv&produit&du&au
GET    /rapports/ventes?pdv&du&au
GET    /rapports/stock-valorise?pdv
```

Conventions : pagination `page`/`size`/`sort`, réponse `{ content, page, size, totalElements, totalPages }`. Dates ISO-8601. Documentation OpenAPI 3.1 via springdoc, exposée sur `/swagger-ui`.

---

## 14. Architecture technique — Backend

### 14.1 Stack

| Élément | Version / choix |
|---|---|
| Java | **25** (LTS) |
| Spring Boot | **4.1.x** (Spring Framework 7, Jakarta EE 11, Jackson 3) |
| Persistance | Spring Data JPA + Hibernate 7 |
| Migrations | **Liquibase** (changelogs XML ou YAML, un fichier par version) |
| Base | **PostgreSQL 17** |
| Cache / sessions | **Redis 7** (Spring Data Redis) |
| Sécurité | Spring Security 6.x, JWT (jjwt ou Nimbus) |
| Mapping | MapStruct |
| Validation | Jakarta Validation (Bean Validation 3.1) |
| PDF | OpenPDF ou Flying Saucer (HTML Thymeleaf → PDF) |
| Build | Maven |
| Observabilité | Spring Boot Actuator + Micrometer + OpenTelemetry |

### 14.2 Découpage en modules Maven

```
obv-gestion/
├── obv-domain/          entités JPA, enums, règles métier pures, VO
├── obv-application/     services, cas d'usage, ports (interfaces)
├── obv-infrastructure/  repositories, adaptateurs SMS/mail/PDF, config Redis
├── obv-api/             contrôleurs REST, DTO, mappers, gestion d'erreurs
└── obv-bootstrap/       application Spring Boot, config, changelogs Liquibase
```

Architecture en couches simple : `Controller → Service → Repository`. Pas de CQRS, pas d'event sourcing, pas de microservices.

### 14.3 Règles techniques

- Utilisation de Redis : cache du référentiel (marques, produits, tarifs — TTL 1 h, invalidation à l'écriture), stockage des paniers, stockage des OTP et refresh tokens, rate limiting.
- Montants : `long` (FCFA entiers) en base, `Money` value object en domaine. Jamais de `double`.
- Toute opération multi-écriture (`stock` + `mouvement` + `document`) dans une seule `@Transactional`.
- `application.yml` par profil : `dev`, `test`, `prod`. Secrets uniquement par variables d'environnement.
- Java 25 : records pour les DTO, sealed interfaces pour les résultats, pattern matching, virtual threads activés (`spring.threads.virtual.enabled=true`).

---

## 15. Frontend — Angular

### 15.1 Version

Angular n'a pas de « version LTS » à cibler pour un nouveau projet : chaque majeure reçoit 6 mois de support actif puis 12 mois de LTS. Pour un développement neuf, cibler la dernière stable en support actif : **Angular 22** (juin 2026 ; Angular 21 est en LTS jusqu'en mai 2027). Vérifier la version courante avec `ng version` au démarrage.

### 15.2 Directives

- Composants **standalone**, **signals** pour l'état, nouvelle syntaxe de contrôle de flux (`@if`, `@for`, `@switch`), **zoneless**.
- `httpResource` / `rxResource` pour les appels API. Intercepteur JWT + refresh automatique.
- Formulaires : Signal Forms.
- Bibliothèque UI : **Angular Material** avec thème personnalisé (§15.4). Pas de mélange de bibliothèques.
- Structure : `core/` (auth, interceptors, guards), `shared/` (composants, pipes), `features/` (referentiel, utilisateurs, reception, vente-depot, vente-bar, transfert, rapports).
- Routes protégées par guards basés sur les permissions renvoyées par `/auth/login`.
- Client API généré depuis l'OpenAPI (`openapi-generator`) pour éviter la dérive front/back.
- Toutes librairies compatible avec la version d'angulars

### 15.3 Ergonomie

- **Mobile-first** : l'usage principal (dépôt, bar) se fait sur téléphone. Cibles tactiles ≥ 48 px.
- Écran de vente : sélection produit en 3 taps max, gros boutons quantité (+/‑ demi-casier), total permanent en pied d'écran.
- Feedback immédiat sur toute action (toast succès / erreur explicite en français).
- Écrans à concevoir : login, activation, tableau de bord, référentiel (CRUD), utilisateurs, réception (saisie + récap + validation), vente dépôt (sélection + panier + client + commande), vente bar (tickets serveurs), transferts, clôture de session, rapports.

### 15.4 Charte graphique

```css
--vert-primaire:    #096A09;   /* barres, en-têtes, actions principales */
--vert-accent:      #1FA055;   /* succès, validations, états positifs */
--orange-primaire:  #DF6D14;   /* actions secondaires, alertes douces */
--orange-fonce:     #CC5500;   /* survol/appui de l'orange, avertissements */
--fond:             #EFEFEF;   /* fond d'application */
--surface:          #FFFFFF;   /* cartes, tableaux */
--texte:            #1B1B1B;
--texte-inverse:    #FFFFFF;
--erreur:           #B3261E;   /* ajouté : aucune des couleurs fournies ne convient au danger */
```

**Règles** : texte blanc sur `--vert-primaire` et `--orange-fonce` uniquement (contraste AA vérifié). `#1FA055` et `#DF6D14` en aplat ne portent pas de texte blanc en petite taille — utiliser `--texte` dessus ou réserver ces couleurs aux bordures/icônes. Rouge réservé aux actions destructrices (annulation, suppression).

---

## 16. Infrastructure

### 16.1 Docker

- `Dockerfile` multi-stage backend (build Maven → runtime JRE 25 slim, utilisateur non-root).
- `Dockerfile` frontend (build Angular → nginx alpine, config gzip + fallback SPA).
- `docker-compose.yml` de développement : postgres, redis, backend, frontend, mailhog (test emails).
- Healthchecks sur tous les services.

### 16.2 Kubernetes

- Manifests ou chart Helm : `Deployment`, `Service`, `Ingress` (TLS), `ConfigMap`, `Secret`, `HorizontalPodAutoscaler`.
- Probes : `/actuator/health/liveness` et `/actuator/health/readiness`.
- PostgreSQL et Redis en dépendances externes (ne pas les déployer dans le cluster en prod).
- `resources.requests/limits` définis. `PodDisruptionBudget` sur le backend.

### 16.3 CI

- GitHub Actions : build + tests + analyse (SpotBugs) + build image + push registry.

---

## 17. Tests et qualité

- **Unitaires** : JUnit 5 + Mockito, sur les services et les règles de gestion. Cible ≥ 80 % sur `obv-domain` et `obv-application`.
- **Intégration** : **Testcontainers** (PostgreSQL + Redis réels). Aucun test sur H2.
- **API** : `@SpringBootTest` + MockMvc/WebTestClient sur les parcours nominaux et les cas d'erreur.
- **Tests obligatoires** : invariant stock = somme des mouvements ; refus de stock négatif ; concurrence sur deux ventes simultanées du même produit ; contre-passation d'annulation ; immuabilité d'une réception validée ; interdiction de valider son propre document ; expiration/usage unique des jetons et OTP.
- **Frontend** : tests unitaires Vitest, tests e2e Playwright sur le parcours vente dépôt.

---

## 18. Phases de développement

| Phase | Contenu | Critère de fin |
|---|---|---|
| **P0** | Squelette monorepo, modules Maven, Liquibase, docker-compose, Actuator, CI | `docker compose up` démarre backend + front + bases |
| **P1** | Authentification, utilisateurs, rôles/permissions, activation SMS/email + OTP | Un SUPER_ADMIN peut créer et activer un gérant |
| **P2** | Référentiel complet + tarification datée | CRUD complet, historisation des prix testée |
| **P3** | Stock + mouvements + réception dépôt (workflow complet, PDF récap) | Réception → clôture → validation → stock incrémenté |
| **P4** | Session de vente dépôt : panier, client, commande, bon de commande, facture, clôture | Journée complète simulable de bout en bout |
| **P5** | Transferts dépôt → bar | Cohérence des deux stocks vérifiée |
| **P6** | Vente bar : tickets serveurs, encaissement, clôture | Journée bar complète |
| **P7** | Rapports, tableau de bord, exports | — |
| **P8** | Kubernetes, observabilité, durcissement | Déploiement reproductible |

---

## 19. Corrections apportées à l'expression de besoin initiale

1. **Suppression physique en base** (annulation de réception) remplacée par annulation logique + contre-passation (RG-18). Une suppression réelle casse l'intégrité référentielle et rend toute comptabilité invérifiable.
2. **Prix attaché au casier** remplacé par une entité `Tarif` datée + prix figé sur les lignes (RG-08, RG-09). Sans cela, changer un prix réécrit l'historique des ventes.
3. **Quantités en 0,5** remplacées par un entier de demi-casiers (RG-11).
4. **OTP de 4 chiffres** porté à 6 avec TTL, limitation de tentatives et rate limiting (RG-03).
5. **Fournisseur** ajouté au modèle (absent).
6. **Transfert dépôt → bar** spécifié (mentionné mais non décrit ; les deux stocks étaient sinon déconnectés).
7. **Session de vente** introduite comme entité de premier plan (la « journée de vente » n'avait pas de support).
8. **Point de vente** introduit pour permettre plusieurs bars sans refonte.
9. **Serveurs** modélisés comme référentiel et non comme comptes applicatifs, avec tickets par serveur (RG-33).
10. **Suppression d'utilisateur** remplacée par archivage (RG-05).
11. **Séparation des tâches** ajoutée : on ne valide pas son propre document (RG-01).
12. **Journal des mouvements de stock** ajouté : sans lui, aucun contrôle ni audit possible.
13. **Idempotence** sur la commande et les liens de validation (RG-27, RG-36).
14. **Contrôle de stock négatif** explicité (RG-15) — absent de la spec initiale.
15. **Angular « dernière version LTS »** clarifié : cibler la dernière stable en support actif (§15.1).

---

## 20. Questions ouvertes à trancher

1. Devise et format d'affichage confirmés (FCFA sans décimale) ? : sans décimal
2. Gestion de la consigne (casiers et bouteilles vides) : à intégrer en v1 ou v2 ? V1
3. Vente à crédit / clients à échéance : nécessaire ? : pas de vente à crédit
4. Modes de paiement à gérer (espèces, mobile money, virement) ? : espèces, mobile money, ou par carte bancaire
5. La facture doit-elle porter des mentions fiscales (n° contribuable, TVA) ? n° contribuable, TVA
6. Combien de bars/maquis rattachés au dépôt ? un seul bar ou maquis pour le moment, mais sa peur évoluer plutard
7. Les serveurs doivent-ils avoir un compte applicatif (saisie autonome) à terme ? oui 
8. Le prix de cession dépôt → bar est-il le prix de vente public ou un tarif interne ? tarif interne
9. Faut-il un inventaire physique périodique avec écart et régularisation ? oui 
10. Volumétrie attendue (lignes de vente/jour, nombre d'utilisateurs) pour dimensionner ? moins de 20 utilisateur par jour, plus de 300 lignes de vente en maquis et dépot par jours
