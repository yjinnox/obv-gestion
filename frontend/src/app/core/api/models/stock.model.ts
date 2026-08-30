import { TypeMouvementStock } from './enums';

export interface StockResponse {
  id: number;
  pointDeVenteId: number;
  produitId: number;
  marqueLibelle: string;
  volumeLibelle: string;
  quantite: number;
}

export interface MouvementStockResponse {
  id: number;
  pointDeVenteId: number;
  produitId: number;
  marqueLibelle: string;
  volumeLibelle: string;
  type: TypeMouvementStock;
  quantiteSignee: number;
  stockAvant: number;
  stockApres: number;
  documentType: string | null;
  documentId: number | null;
  dateHeure: string;
  utilisateurId: number;
}
