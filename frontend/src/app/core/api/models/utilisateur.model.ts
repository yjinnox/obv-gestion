export type CanalContact = 'EMAIL' | 'TELEPHONE';
export type StatutUtilisateur = 'EN_ATTENTE_ACTIVATION' | 'ACTIF' | 'DESACTIVE' | 'ARCHIVE';
export type RoleUtilisateur = 'SUPER_ADMINISTRATEUR' | 'ADMINISTRATEUR' | 'GERANT_DEPOT' | 'GERANT_BAR' | 'VENDEUR';

export const LIBELLES_ROLE: Record<RoleUtilisateur, string> = {
  SUPER_ADMINISTRATEUR: 'Super administrateur',
  ADMINISTRATEUR: 'Administrateur',
  GERANT_DEPOT: 'Gérant dépôt',
  GERANT_BAR: 'Gérant bar',
  VENDEUR: 'Vendeur',
};

export const LIBELLES_STATUT_UTILISATEUR: Record<StatutUtilisateur, string> = {
  EN_ATTENTE_ACTIVATION: "En attente d'activation",
  ACTIF: 'Actif',
  DESACTIVE: 'Désactivé',
  ARCHIVE: 'Archivé',
};

/** Rôles nécessitant un point de vente (voir RoleUtilisateur.Portee côté domaine). */
export const ROLES_AVEC_POINT_DE_VENTE: RoleUtilisateur[] = ['GERANT_DEPOT', 'GERANT_BAR', 'VENDEUR'];

/** Type de point de vente imposé par rôle (voir RoleUtilisateur.typePointDeVenteRequis côté domaine). */
export const TYPE_POINT_DE_VENTE_REQUIS: Partial<Record<RoleUtilisateur, 'DEPOT' | 'BAR'>> = {
  GERANT_DEPOT: 'DEPOT',
  GERANT_BAR: 'BAR',
};

export interface AffectationResponse {
  id: number;
  role: RoleUtilisateur;
  pointDeVenteId: number | null;
}
export interface AffectationRequest {
  role: RoleUtilisateur;
  pointDeVenteId?: number | null;
}

export interface UtilisateurResponse {
  id: number;
  nom: string;
  prenoms: string;
  canalContact: CanalContact;
  email: string | null;
  telephone: string | null;
  statut: StatutUtilisateur;
  affectations: AffectationResponse[];
}

export interface CreerUtilisateurRequest {
  nom: string;
  prenoms: string;
  canalContact: CanalContact;
  email?: string | null;
  telephone?: string | null;
  affectations: AffectationRequest[];
}

export interface ModifierUtilisateurRequest {
  nom: string;
  prenoms: string;
}
