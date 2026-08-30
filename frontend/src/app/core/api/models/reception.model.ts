import { StatutReception } from './enums';

export interface LigneReceptionResponse {
  id: number;
  produitId: number;
  marqueLibelle: string;
  volumeLibelle: string;
  conditionnementId: number;
  capaciteBouteilles: number;
  nombreCasiers: number;
  prixAchatCasierXof: number;
  montantLigneXof: number;
}

export interface ReceptionResponse {
  id: number;
  fournisseurId: number;
  fournisseurRaisonSociale: string;
  pointDeVenteId: number;
  pointDeVenteLibelle: string;
  dateHeureLivraison: string;
  statut: StatutReception;
  motifAnnulation: string | null;
  clotureePar: string | null;
  lignes: LigneReceptionResponse[];
  montantTotalXof: number;
}

export interface RecapitulatifReceptionResponse {
  reception: ReceptionResponse;
  totalParMarqueXof: Record<string, number>;
  totalParVolumeXof: Record<string, number>;
  montantTotalXof: number;
}

export interface CreerReceptionRequest {
  fournisseurId: number;
  pointDeVenteId: number;
  dateHeureLivraison: string;
}

export interface AjouterLigneReceptionRequest {
  produitId: number;
  conditionnementId: number;
  nombreCasiers: number;
  prixAchatCasierXof?: number | null;
}

export interface ModifierLigneReceptionRequest {
  nombreCasiers: number;
  prixAchatCasierXof: number;
}

export interface AnnulerReceptionRequest {
  motif: string;
}

export interface DemanderValidationRequest {
  destinataireId: number;
}
