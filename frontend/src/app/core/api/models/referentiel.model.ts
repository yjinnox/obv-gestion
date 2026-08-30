import { NatureTarif, TypeClient, TypePointDeVente, UniteVente } from './enums';

export interface MarqueResponse {
  id: number;
  libelle: string;
  actif: boolean;
}
export interface CreerMarqueRequest {
  libelle: string;
}
export interface ModifierMarqueRequest {
  libelle: string;
  actif: boolean;
}

export interface VolumeResponse {
  id: number;
  libelle: string;
  contenanceMl: number;
  actif: boolean;
}
export interface CreerVolumeRequest {
  libelle: string;
  contenanceMl: number;
}
export interface ModifierVolumeRequest {
  libelle: string;
  contenanceMl: number;
  actif: boolean;
}

export interface ProduitResponse {
  id: number;
  marqueId: number;
  marqueLibelle: string;
  volumeId: number;
  volumeLibelle: string;
  montantConsigneXof: number;
  actif: boolean;
}
export interface CreerProduitRequest {
  marqueId: number;
  volumeId: number;
}
export interface ModifierProduitRequest {
  montantConsigneXof: number;
  actif: boolean;
}

export interface ConditionnementResponse {
  id: number;
  produitId: number;
  capaciteBouteilles: number;
  demiCasierAutorise: boolean;
  actif: boolean;
}
export interface CreerConditionnementRequest {
  produitId: number;
  capaciteBouteilles: number;
}
export interface ModifierConditionnementRequest {
  actif: boolean;
}

export interface PointDeVenteResponse {
  id: number;
  libelle: string;
  type: TypePointDeVente;
  adresse: string;
  actif: boolean;
}
export interface CreerPointDeVenteRequest {
  libelle: string;
  type: TypePointDeVente;
  adresse: string;
}
export interface ModifierPointDeVenteRequest {
  libelle: string;
  adresse: string;
  actif: boolean;
}

export interface FournisseurResponse {
  id: number;
  raisonSociale: string;
  telephone: string;
  email: string;
  adresse: string;
  actif: boolean;
}
export interface CreerFournisseurRequest {
  raisonSociale: string;
  telephone: string;
  email: string;
  adresse: string;
}
export interface ModifierFournisseurRequest {
  raisonSociale: string;
  telephone: string;
  email: string;
  adresse: string;
  actif: boolean;
}

export interface ClientResponse {
  id: number;
  type: TypeClient;
  nom: string;
  prenoms: string;
  raisonSociale: string;
  telephone: string;
  email: string;
  adresseFacturation: string;
  actif: boolean;
}
export interface CreerClientRequest {
  type: TypeClient;
  nom?: string;
  prenoms?: string;
  raisonSociale?: string;
  telephone: string;
  email?: string;
  adresseFacturation?: string;
}
export interface ModifierClientRequest {
  telephone: string;
  email: string;
  adresseFacturation: string;
  actif: boolean;
}

export interface ServeurResponse {
  id: number;
  pointDeVenteId: number;
  nom: string;
  prenoms: string;
  telephone: string;
  actif: boolean;
}
export interface CreerServeurRequest {
  pointDeVenteId: number;
  nom: string;
  prenoms: string;
  telephone: string;
}
export interface ModifierServeurRequest {
  nom: string;
  prenoms: string;
  telephone: string;
  actif: boolean;
}

export interface TarifResponse {
  id: number;
  pointDeVenteId: number;
  produitId: number;
  uniteVente: UniteVente;
  nature: NatureTarif;
  montantXof: number;
  dateDebut: string;
  dateFin: string | null;
}
export interface CreerTarifRequest {
  pointDeVenteId: number;
  produitId: number;
  uniteVente: UniteVente;
  nature: NatureTarif;
  montantXof: number;
  dateDebut: string;
}
