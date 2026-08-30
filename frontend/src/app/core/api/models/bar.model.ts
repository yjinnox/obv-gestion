import { ModePaiement, StatutTicketServeur } from './enums';
import { SessionVenteResponse } from './vente.model';

export interface LigneTicketServeurResponse {
  id: number;
  produitId: number;
  marqueLibelle: string;
  volumeLibelle: string;
  quantiteBouteilles: number;
  prixVenteBouteilleXof: number;
  montantLigneXof: number;
}

export interface TicketServeurResponse {
  id: number;
  sessionVenteId: number;
  serveurId: number;
  serveurNom: string;
  serveurPrenoms: string;
  statut: StatutTicketServeur;
  modePaiement: ModePaiement | null;
  montantTotalXof: number;
  dateEncaissement: string | null;
  encaisseePar: string | null;
  lignes: LigneTicketServeurResponse[];
}

export interface RecapitulatifSessionBarResponse {
  session: SessionVenteResponse;
  quantiteParServeurBouteilles: Record<string, number>;
  quantiteParMarqueBouteilles: Record<string, number>;
  quantiteParVolumeBouteilles: Record<string, number>;
  quantiteTotaleBouteilles: number;
  recetteTotaleXof: number;
}

export interface CreerTicketServeurRequest {
  sessionVenteId: number;
  serveurId: number;
}
export interface AjouterLigneTicketRequest {
  produitId: number;
  quantiteBouteilles: number;
  prixVenteBouteilleXof?: number | null;
}
export interface EncaisserTicketRequest {
  modePaiement: ModePaiement;
}
export interface ModifierLigneTicketRequest {
  quantiteBouteilles: number;
}
