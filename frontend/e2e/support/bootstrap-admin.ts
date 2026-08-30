import type { APIRequestContext } from '@playwright/test';
import { Client } from 'pg';

interface OptionsBootstrap {
  request: APIRequestContext;
  email: string;
  motDePasse: string;
}

/**
 * Récupère la dernière notification d'un gabarit donné pour ce destinataire.
 * §11 — le pattern outbox (`notification_outbox.variables_json`) conserve les
 * variables en clair (lien d'activation, code OTP) indépendamment de la
 * réussite de l'envoi réel (SMTP absent en CI), ce qui en fait la seule
 * source fiable pour un bootstrap de test sans boîte mail.
 */
async function derniereNotification(
  client: Client,
  destinataire: string,
  gabarit: string,
): Promise<Record<string, string>> {
  const { rows } = await client.query<{ variables_json: string }>(
    `select variables_json from notification_outbox
     where destinataire = $1 and gabarit = $2
     order by id desc limit 1`,
    [destinataire, gabarit],
  );
  if (rows.length === 0) {
    throw new Error(`Aucune notification "${gabarit}" trouvée pour ${destinataire} (notification_outbox vide).`);
  }
  return JSON.parse(rows[0].variables_json) as Record<string, string>;
}

async function connecter(request: APIRequestContext, email: string, motDePasse: string) {
  return request.post('/api/v1/auth/login', { data: { identifiant: email, motDePasse } });
}

function nouveauClientPostgres(): Client {
  return new Client({
    host: process.env['E2E_DB_HOST'] ?? 'localhost',
    port: Number(process.env['E2E_DB_PORT'] ?? 5432),
    database: process.env['E2E_DB_NAME'] ?? 'obv_gestion',
    user: process.env['E2E_DB_USER'] ?? 'obv',
    password: process.env['E2E_DB_PASSWORD'] ?? 'obv',
  });
}

/**
 * Complète le parcours d'activation (§4.2) d'un compte déjà invité, en
 * lisant le jeton et l'OTP directement en base plutôt que par email.
 */
async function activerParOutbox(request: APIRequestContext, email: string, motDePasse: string): Promise<void> {
  const client = nouveauClientPostgres();
  await client.connect();
  try {
    const { lienActivation } = await derniereNotification(client, email, 'invitation-activation');
    const token = new URL(lienActivation).searchParams.get('token');
    if (!token) {
      throw new Error(`Lien d'activation sans paramètre "token" : ${lienActivation}`);
    }

    const reponseMotDePasse = await request.post(`/api/v1/auth/activation/${token}/mot-de-passe`, {
      data: { motDePasse, confirmation: motDePasse },
    });
    if (!reponseMotDePasse.ok()) {
      throw new Error(`Échec de définition du mot de passe (HTTP ${reponseMotDePasse.status()}).`);
    }

    const { code } = await derniereNotification(client, email, 'otp');
    const reponseOtp = await request.post(`/api/v1/auth/activation/${token}/otp`, { data: { code } });
    if (!reponseOtp.ok()) {
      throw new Error(`Échec de validation de l'OTP (HTTP ${reponseOtp.status()}).`);
    }
  } finally {
    await client.end();
  }
}

/**
 * Garantit qu'un compte est authentifiable, en l'activant au besoin (§4.2).
 * Chemin rapide : le mot de passe donné fonctionne déjà (base de dev
 * persistante réutilisée d'une exécution à l'autre). Sinon (base CI neuve,
 * compte fraîchement amorcé par BootstrapSuperAdministrateurRunner) : suit
 * le parcours d'activation complet via `activerParOutbox`.
 */
export async function assurerCompteActif({ request, email, motDePasse }: OptionsBootstrap): Promise<string> {
  const connexionRapide = await connecter(request, email, motDePasse);
  if (connexionRapide.ok()) {
    return ((await connexionRapide.json()) as { accessToken: string }).accessToken;
  }

  await activerParOutbox(request, email, motDePasse);

  const connexionFinale = await connecter(request, email, motDePasse);
  if (!connexionFinale.ok()) {
    throw new Error(`Connexion toujours en échec après activation (HTTP ${connexionFinale.status()}).`);
  }
  return ((await connexionFinale.json()) as { accessToken: string }).accessToken;
}

/**
 * Crée un second SUPER_ADMINISTRATEUR et l'active, puis retourne son jeton
 * d'accès. RG-01 (séparation des tâches) interdit à un même utilisateur de
 * clôturer ET valider un même document (réception, transfert...) : le
 * parcours vente dépôt a besoin de stock, donc d'une réception validée, ce
 * qui exige un second compte distinct de celui utilisé pour la créer.
 */
export async function creerEtActiverValidateur(
  request: APIRequestContext,
  accessTokenCreateur: string,
  suffixe: string,
): Promise<string> {
  const email = `validateur-e2e-${suffixe}@obv-gestion.local`;
  const reponse = await request.post('/api/v1/utilisateurs', {
    headers: { Authorization: `Bearer ${accessTokenCreateur}` },
    data: {
      nom: 'ValidateurE2E',
      prenoms: suffixe,
      canalContact: 'EMAIL',
      email,
      affectations: [{ role: 'SUPER_ADMINISTRATEUR', pointDeVenteId: null }],
    },
  });
  if (!reponse.ok()) {
    throw new Error(`Échec de création du validateur e2e (HTTP ${reponse.status()}) : ${await reponse.text()}`);
  }

  const motDePasse = 'DevTest1234!';
  await activerParOutbox(request, email, motDePasse);
  const connexion = await connecter(request, email, motDePasse);
  if (!connexion.ok()) {
    throw new Error(`Connexion du validateur e2e en échec (HTTP ${connexion.status()}).`);
  }
  return ((await connexion.json()) as { accessToken: string }).accessToken;
}
