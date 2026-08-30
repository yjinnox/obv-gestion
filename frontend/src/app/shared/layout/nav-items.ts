import { Permission } from '../../core/auth/auth.model';

export interface NavItem {
  libelle: string;
  route: string;
  icone: string;
  /** Non renseigné = visible pour tout utilisateur authentifié. */
  permission?: Permission;
}

/** Entrées de navigation principale, filtrées par permission (§15.2). */
export const NAV_ITEMS: NavItem[] = [
  { libelle: 'Tableau de bord', route: '/tableau-de-bord', icone: 'dashboard' },
  { libelle: 'Vente', route: '/vente', icone: 'point_of_sale', permission: 'VENTE_WRITE' },
  { libelle: 'Tickets serveurs', route: '/tickets-serveur', icone: 'sports_bar', permission: 'VENTE_WRITE' },
  { libelle: 'Réceptions', route: '/receptions', icone: 'local_shipping', permission: 'RECEPTION_READ' },
  { libelle: 'Stocks', route: '/stocks', icone: 'inventory_2', permission: 'REFERENTIEL_READ' },
  { libelle: 'Transferts', route: '/transferts', icone: 'sync_alt', permission: 'TRANSFERT_WRITE' },
  { libelle: 'Rapports', route: '/rapports', icone: 'bar_chart', permission: 'RAPPORT_READ' },
  { libelle: 'Référentiel', route: '/referentiel', icone: 'category', permission: 'REFERENTIEL_READ' },
  { libelle: 'Utilisateurs', route: '/utilisateurs', icone: 'group', permission: 'UTILISATEUR_READ' },
];
