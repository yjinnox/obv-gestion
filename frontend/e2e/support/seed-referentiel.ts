import type { APIRequestContext } from '@playwright/test';

export interface DonneesSeedVenteDepot {
  pointDeVenteLibelle: string;
  produitLibelleAttendu: string;
}

async function appelerPost<T>(
  request: APIRequestContext,
  url: string,
  accessToken: string,
  data: Record<string, unknown>,
): Promise<T> {
  const reponse = await request.post(url, {
    headers: { Authorization: `Bearer ${accessToken}` },
    data,
  });
  if (!reponse.ok()) {
    throw new Error(`Échec POST ${url} (HTTP ${reponse.status()}) : ${await reponse.text()}`);
  }
  if (reponse.status() === 204) {
    return undefined as T;
  }
  return reponse.json() as Promise<T>;
}

/**
 * Sème un point de vente DEPOT, un produit, un tarif VENTE actif et un
 * stock suffisant (via une réception validée), isolés par un suffixe
 * unique par exécution. RG-24 n'empêche que l'AJOUT au panier sans stock
 * suffisant (informatif) ; VenteService rejette en revanche la commande
 * elle-même (409 STOCK_INSUFFISANT) si le stock manque au moment de
 * commander — d'où la réception ci-dessous.
 */
export async function semerReferentielVenteDepot(
  request: APIRequestContext,
  accessToken: string,
  accessTokenValidateur: string,
): Promise<DonneesSeedVenteDepot> {
  const suffixe = Date.now().toString(36);

  const marque = await appelerPost<{ id: number; libelle: string }>(request, '/api/v1/marques', accessToken, {
    libelle: `MarqueE2E-${suffixe}`,
  });
  const volume = await appelerPost<{ id: number; libelle: string }>(request, '/api/v1/volumes', accessToken, {
    libelle: `VolE2E-${suffixe}`,
    contenanceMl: 650,
  });
  const produit = await appelerPost<{ id: number }>(request, '/api/v1/produits', accessToken, {
    marqueId: marque.id,
    volumeId: volume.id,
  });
  const pointDeVente = await appelerPost<{ id: number; libelle: string }>(request, '/api/v1/points-de-vente', accessToken, {
    libelle: `DepotE2E-${suffixe}`,
    type: 'DEPOT',
    adresse: 'Zone e2e',
  });
  // RG-13 : le demi-casier n'est autorisé que si la capacité du
  // conditionnement est paire (PanierService la vérifie à l'ajout).
  const conditionnement = await appelerPost<{ id: number }>(request, '/api/v1/conditionnements', accessToken, {
    produitId: produit.id,
    capaciteBouteilles: 12,
  });
  await appelerPost(request, '/api/v1/tarifs', accessToken, {
    pointDeVenteId: pointDeVente.id,
    produitId: produit.id,
    uniteVente: 'CASIER',
    nature: 'VENTE',
    montantXof: 5000,
    dateDebut: new Date().toISOString().slice(0, 10),
  });

  const fournisseur = await appelerPost<{ id: number }>(request, '/api/v1/fournisseurs', accessToken, {
    raisonSociale: `FournisseurE2E-${suffixe}`,
    telephone: '0700000000',
  });
  const reception = await appelerPost<{ id: number }>(request, '/api/v1/receptions', accessToken, {
    fournisseurId: fournisseur.id,
    pointDeVenteId: pointDeVente.id,
    dateHeureLivraison: new Date().toISOString(),
  });
  await appelerPost(request, `/api/v1/receptions/${reception.id}/lignes`, accessToken, {
    produitId: produit.id,
    conditionnementId: conditionnement.id,
    nombreCasiers: 5,
    prixAchatCasierXof: 3000,
  });
  await appelerPost(request, `/api/v1/receptions/${reception.id}/cloturer`, accessToken, {});
  // RG-01 : qui clôture ne peut pas valider — un second compte est requis.
  await appelerPost(request, `/api/v1/receptions/${reception.id}/valider`, accessTokenValidateur, {});

  return {
    pointDeVenteLibelle: pointDeVente.libelle,
    produitLibelleAttendu: `${marque.libelle} ${volume.libelle}`,
  };
}
