// Énumérations métier partagées, alignées sur obv-domain (voir api-reference).

export type StatutReception = 'BROUILLON' | 'EN_ATTENTE_VALIDATION' | 'VALIDEE' | 'ANNULEE';
export type StatutTransfert = 'BROUILLON' | 'EN_ATTENTE_VALIDATION' | 'VALIDEE' | 'ANNULEE';
export type StatutSessionVente = 'OUVERTE' | 'CLOTUREE' | 'EN_MODIFICATION' | 'VALIDEE';
export type StatutTicketServeur = 'OUVERT' | 'ENCAISSE';
export type ModePaiement = 'ESPECES' | 'MOBILE_MONEY' | 'CARTE_BANCAIRE';
export type TypeClient = 'PARTICULIER' | 'ENTREPRISE';
export type TypePointDeVente = 'DEPOT' | 'BAR';
export type NatureTarif = 'ACHAT' | 'VENTE' | 'CESSION';
export type UniteVente = 'CASIER' | 'BOUTEILLE';
export type TypeMouvementStock =
  | 'ENTREE_RECEPTION'
  | 'SORTIE_VENTE'
  | 'SORTIE_TRANSFERT'
  | 'ENTREE_TRANSFERT'
  | 'AJUSTEMENT'
  | 'CONTRE_PASSATION';

export const LIBELLES_MODE_PAIEMENT: Record<ModePaiement, string> = {
  ESPECES: 'Espèces',
  MOBILE_MONEY: 'Mobile money',
  CARTE_BANCAIRE: 'Carte bancaire',
};

export const LIBELLES_STATUT_RECEPTION: Record<StatutReception, string> = {
  BROUILLON: 'Brouillon',
  EN_ATTENTE_VALIDATION: 'En attente de validation',
  VALIDEE: 'Validée',
  ANNULEE: 'Annulée',
};

export const LIBELLES_STATUT_TRANSFERT: Record<StatutTransfert, string> = {
  BROUILLON: 'Brouillon',
  EN_ATTENTE_VALIDATION: 'En attente de validation',
  VALIDEE: 'Validé',
  ANNULEE: 'Annulé',
};

export const LIBELLES_STATUT_SESSION_VENTE: Record<StatutSessionVente, string> = {
  OUVERTE: 'Ouverte',
  CLOTUREE: 'Clôturée',
  EN_MODIFICATION: 'En modification',
  VALIDEE: 'Validée',
};

export const LIBELLES_STATUT_TICKET: Record<StatutTicketServeur, string> = {
  OUVERT: 'Ouvert',
  ENCAISSE: 'Encaissé',
};
