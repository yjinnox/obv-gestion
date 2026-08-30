import { TypePointDeVente } from './enums';

export interface RapportVentesResponse {
  pointDeVenteId: number;
  pointDeVenteLibelle: string;
  pointDeVenteType: TypePointDeVente;
  periodeDu: string | null;
  periodeAu: string | null;
  quantiteTotale: number;
  quantiteParMarque: Record<string, number>;
  quantiteParVolume: Record<string, number>;
  recetteParModePaiementXof: Record<string, number>;
  quantiteParServeur: Record<string, number>;
  recetteParJourXof: Record<string, number>;
  recetteTotaleXof: number;
}

export interface LigneStockValoriseResponse {
  pointDeVenteId: number;
  pointDeVenteLibelle: string;
  produitId: number;
  marqueLibelle: string;
  volumeLibelle: string;
  quantite: number;
  prixAchatCasierXof: number | null;
  valeurLigneXof: number | null;
}

export interface RapportStockValoriseResponse {
  lignes: LigneStockValoriseResponse[];
  valeurTotaleXof: number;
}
