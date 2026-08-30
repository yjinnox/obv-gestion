import { ModePaiement, StatutSessionVente } from './enums';
import { CreerClientRequest } from './referentiel.model';

export interface LignePanierResponse {
  id: number;
  produitId: number;
  marqueLibelle: string;
  volumeLibelle: string;
  quantiteDemiCasiers: number;
  prixVenteCasierXof: number;
  montantLigneXof: number;
}
export interface PanierResponse {
  utilisateurId: number;
  sessionVenteId: number;
  lignes: LignePanierResponse[];
  montantGlobalXof: number;
}
export interface AjoutPanierResponse {
  stockDisponibleDemiCasiers: number;
}
export interface AjouterLignePanierRequest {
  sessionVenteId: number;
  produitId: number;
  quantiteDemiCasiers: number;
}
export interface ModifierLignePanierRequest {
  sessionVenteId: number;
  quantiteDemiCasiers: number;
}

export interface SessionVenteResponse {
  id: number;
  pointDeVenteId: number;
  pointDeVenteLibelle: string;
  dateOuverture: string;
  ouvertePar: string;
  fondCaisseXof: number;
  statut: StatutSessionVente;
  dateCloture: string | null;
  clotureePar: string | null;
  totalTheoriqueXof: number | null;
  totalCompteXof: number | null;
  ecartXof: number | null;
  dateValidation: string | null;
  valideePar: string | null;
}
export interface RecapitulatifSessionVenteResponse {
  session: SessionVenteResponse;
  quantiteParMarqueDemiCasiers: Record<string, number>;
  quantiteParVolumeDemiCasiers: Record<string, number>;
  quantiteTotaleDemiCasiers: number;
  recetteParModePaiementXof: Record<string, number>;
  recetteTotaleXof: number;
}
export interface OuvrirSessionVenteRequest {
  pointDeVenteId: number;
  fondCaisseXof: number;
}
export interface ClotureSessionVenteRequest {
  totalCompteXof: number;
}

export interface LigneVenteResponse {
  id: number;
  produitId: number;
  marqueLibelle: string;
  volumeLibelle: string;
  quantiteDemiCasiers: number;
  prixVenteCasierXof: number;
  montantConsigneCasierXof: number;
  montantLigneXof: number;
}
export interface VenteResponse {
  id: number;
  sessionVenteId: number;
  clientId: number | null;
  clientNom: string | null;
  numeroBonCommande: string;
  numeroFacture: string;
  modePaiement: ModePaiement;
  montantSousTotalXof: number;
  montantConsigneXof: number;
  montantTvaXof: number;
  montantTotalXof: number;
  dateHeure: string;
  lignes: LigneVenteResponse[];
}
export interface CreerCommandeRequest {
  sessionVenteId: number;
  clientId?: number | null;
  nouveauClient?: CreerClientRequest | null;
  modePaiement: ModePaiement;
}
export interface ModifierQuantiteLigneVenteRequest {
  quantiteDemiCasiers: number;
}
