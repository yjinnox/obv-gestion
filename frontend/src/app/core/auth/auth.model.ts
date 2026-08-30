/** Réponse de connexion/rafraîchissement (voir AuthController#versReponse). */
export interface ConnexionResponse {
  accessToken: string;
  refreshToken: string;
  utilisateurId: number;
  permissions: string[];
}

/** Permissions granulaires (voir domain.utilisateur.Permission). */
export type Permission =
  | 'REFERENTIEL_READ'
  | 'REFERENTIEL_WRITE'
  | 'UTILISATEUR_READ'
  | 'UTILISATEUR_WRITE'
  | 'RECEPTION_READ'
  | 'RECEPTION_WRITE'
  | 'RECEPTION_VALIDER'
  | 'VENTE_READ'
  | 'VENTE_WRITE'
  | 'SESSION_CLOTURER'
  | 'SESSION_VALIDER'
  | 'TRANSFERT_WRITE'
  | 'TRANSFERT_VALIDER'
  | 'CLIENT_READ'
  | 'CLIENT_WRITE'
  | 'RAPPORT_READ'
  | 'MODIFICATION_POST_CLOTURE';

/** Session persistée en stockage local entre rechargements de page. */
export interface SessionPersistee {
  accessToken: string;
  refreshToken: string;
  utilisateurId: number;
  permissions: Permission[];
}
