import { test, expect, type Page } from '@playwright/test';
import { assurerCompteActif, creerEtActiverValidateur } from './support/bootstrap-admin';
import { semerReferentielVenteDepot, type DonneesSeedVenteDepot } from './support/seed-referentiel';

/**
 * §17 — parcours vente dépôt de bout en bout : connexion, ouverture de
 * session, sélection produit en 3 taps max (§15.3), commande et
 * confirmation avec justificatifs PDF.
 *
 * Identifiants configurables via E2E_ADMIN_EMAIL / E2E_ADMIN_PASSWORD (par
 * défaut : le compte SUPER_ADMINISTRATEUR amorcé par
 * BootstrapSuperAdministrateurRunner en dev). Sur une base neuve (CI), le
 * compte n'est pas encore activé : `assurerCompteActif` complète alors le
 * parcours d'activation (§4.2) via `notification_outbox` — voir
 * e2e/support/bootstrap-admin.ts. La connexion à Postgres pour ce
 * bootstrap se configure via E2E_DB_HOST/PORT/NAME/USER/PASSWORD.
 */

const EMAIL_ADMIN = process.env['E2E_ADMIN_EMAIL'] ?? 'admin@obv-gestion.local';
const MOT_DE_PASSE_ADMIN = process.env['E2E_ADMIN_PASSWORD'] ?? 'DevTest1234!';

// §15.3 — l'écran de vente est conçu mobile-first (sélection produit en 3
// taps max, pied d'écran permanent) : on teste au gabarit réellement utilisé
// au comptoir plutôt qu'au viewport desktop par défaut.
test.use({ viewport: { width: 390, height: 844 } });

let donnees: DonneesSeedVenteDepot;

test.beforeAll(async ({ request }) => {
  const accessToken = await assurerCompteActif({ request, email: EMAIL_ADMIN, motDePasse: MOT_DE_PASSE_ADMIN });
  // RG-01 (séparation des tâches) : la réception qui fournit le stock doit
  // être validée par un compte distinct de celui qui l'a clôturée.
  const accessTokenValidateur = await creerEtActiverValidateur(request, accessToken, Date.now().toString(36));
  donnees = await semerReferentielVenteDepot(request, accessToken, accessTokenValidateur);
});

async function choisirOption(page: Page, libelleChamp: string, texteOption: string): Promise<void> {
  const champ = page.locator('mat-form-field', { hasText: libelleChamp }).first();
  await champ.locator('mat-select').click({ force: true });
  await page.waitForTimeout(300);
  await page.locator('mat-option', { hasText: texteOption }).click();
  // Laisse l'overlay CDK du mat-select se détacher : sans ce délai, son
  // backdrop peut rester dans le DOM et intercepter un clic ultérieur
  // ailleurs sur la page.
  await page.waitForTimeout(300);
}

test('connexion, ouverture de session, ajout au panier et commande', async ({ page }) => {
  await page.goto('/connexion');
  await page.locator('input[autocomplete="username"]').fill(EMAIL_ADMIN);
  await page.locator('input[autocomplete="current-password"]').fill(MOT_DE_PASSE_ADMIN);
  await page.locator('button[type=submit]').click();
  await page.waitForURL('**/tableau-de-bord');

  await page.goto('/vente');
  await choisirOption(page, 'Point de vente', donnees.pointDeVenteLibelle);

  // §8.1 — un dépôt fraîchement créé n'a pas de session ouverte : le
  // formulaire d'ouverture s'affiche à la place de la grille produits.
  const boutonOuvrir = page.locator('button:has-text("Ouvrir la session")');
  await expect(boutonOuvrir).toBeVisible({ timeout: 5_000 });
  await choisirOption(page, 'Point de vente', donnees.pointDeVenteLibelle);
  await page.locator('mat-form-field', { hasText: 'Fond de caisse' }).locator('input').fill('10000');
  await boutonOuvrir.click();

  // §15.3 — sélection produit en 3 taps max : la carte du produit puis deux
  // taps sur le stepper "+" pour porter le panier à 2 demi-casiers.
  const carteProduit = page.locator('mat-card.produit-carte', { hasText: donnees.produitLibelleAttendu });
  await expect(carteProduit).toBeVisible({ timeout: 5_000 });
  const boutonAjouter = carteProduit.locator('button[aria-label="Ajouter un demi-casier"]');
  const quantite = carteProduit.locator('.produit-carte__quantite');
  await boutonAjouter.click();
  await expect(quantite).toHaveText('1');
  await boutonAjouter.click();
  await expect(quantite).toHaveText('2');

  await page.locator('button:has-text("Voir le panier")').click();

  // Laisse l'animation d'ouverture du bottom sheet (§15.3, feuille en pied
  // d'écran) se terminer avant d'interagir avec son contenu.
  await expect(page.locator('app-checkout-sheet')).toBeVisible();
  await page.waitForTimeout(500);

  // RG-07 — la vente dépôt exige toujours un client (contrairement à la
  // vente bar, anonyme) : on en crée un nouveau pour ne dépendre d'aucune
  // donnée référentiel préexistante.
  await choisirOption(page, 'Client', 'Nouveau client');
  // "Nom" en hasText matcherait aussi "Prénoms" (sous-chaîne) : on cible
  // plutôt le nom accessible exact de chaque champ.
  await page.getByRole('textbox', { name: 'Nom', exact: true }).fill('ClientE2E');
  await page.getByRole('textbox', { name: 'Prénoms', exact: true }).fill('Test');
  // Numéro unique par exécution : le téléphone est unique en base (RG-07),
  // et les clients créés par le test persistent d'une exécution à l'autre.
  await page.getByRole('textbox', { name: 'Téléphone', exact: true }).fill(`07${Date.now().toString().slice(-8)}`);

  await choisirOption(page, 'Mode de paiement', 'Espèces');

  await page.locator('button:has-text("Commander")').click();

  const confirmation = page.locator('.confirmation');
  await expect(confirmation).toBeVisible({ timeout: 10_000 });
  await expect(confirmation.locator('h2')).toContainText('enregistrée');
  await expect(confirmation.getByRole('button', { name: 'Bon de commande' })).toBeVisible();
  await expect(confirmation.getByRole('button', { name: 'Facture' })).toBeVisible();
});
