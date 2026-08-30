import { StatutTransfert } from './enums';

export interface LigneTransfertResponse {
  id: number;
  produitId: number;
  marqueLibelle: string;
  volumeLibelle: string;
  conditionnementId: number;
  capaciteBouteilles: number;
  quantiteDemiCasiers: number;
  quantiteBouteilles: number;
  prixCessionCasierXof: number;
  montantLigneXof: number;
}

export interface BonTransfertResponse {
  id: number;
  numero: string;
  pointDeVenteSourceId: number;
  pointDeVenteSourceLibelle: string;
  pointDeVenteDestinationId: number;
  pointDeVenteDestinationLibelle: string;
  dateHeure: string;
  statut: StatutTransfert;
  motifAnnulation: string | null;
  clotureePar: string | null;
  lignes: LigneTransfertResponse[];
  montantTotalXof: number;
}

export interface LigneTransfertRequest {
  produitId: number;
  conditionnementId: number;
  quantiteDemiCasiers: number;
  prixCessionCasierXof?: number | null;
}

export interface CreerBonTransfertRequest {
  pointDeVenteSourceId: number;
  pointDeVenteDestinationId: number;
  dateHeure: string;
  lignes: LigneTransfertRequest[];
}

export interface AnnulerTransfertRequest {
  motif: string;
}
