import { Routes } from '@angular/router';
import { AuthLayoutComponent } from './shared/layout/auth-layout/auth-layout.component';
import { MainLayoutComponent } from './shared/layout/main-layout/main-layout.component';
import { authGuard, invitesGuard, permissionGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: '',
    component: AuthLayoutComponent,
    canMatch: [invitesGuard],
    children: [
      {
        path: 'connexion',
        loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent),
      },
      {
        path: 'activation',
        loadComponent: () =>
          import('./features/auth/activation/activation.component').then((m) => m.ActivationComponent),
      },
    ],
  },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'tableau-de-bord' },
      {
        path: 'tableau-de-bord',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
      },
      {
        path: 'referentiel',
        canActivate: [permissionGuard],
        data: { permission: 'REFERENTIEL_READ' },
        loadComponent: () =>
          import('./features/referentiel/referentiel.component').then((m) => m.ReferentielComponent),
        children: [
          { path: '', pathMatch: 'full', redirectTo: 'marques' },
          {
            path: 'marques',
            loadComponent: () =>
              import('./features/referentiel/marques/marques.component').then((m) => m.MarquesComponent),
          },
          {
            path: 'volumes',
            loadComponent: () =>
              import('./features/referentiel/volumes/volumes.component').then((m) => m.VolumesComponent),
          },
          {
            path: 'produits',
            loadComponent: () =>
              import('./features/referentiel/produits/produits.component').then((m) => m.ProduitsComponent),
          },
          {
            path: 'conditionnements',
            loadComponent: () =>
              import('./features/referentiel/conditionnements/conditionnements.component').then(
                (m) => m.ConditionnementsComponent,
              ),
          },
          {
            path: 'tarifs',
            loadComponent: () =>
              import('./features/referentiel/tarifs/tarifs.component').then((m) => m.TarifsComponent),
          },
          {
            path: 'points-de-vente',
            loadComponent: () =>
              import('./features/referentiel/points-de-vente/points-de-vente.component').then(
                (m) => m.PointsDeVenteComponent,
              ),
          },
          {
            path: 'fournisseurs',
            loadComponent: () =>
              import('./features/referentiel/fournisseurs/fournisseurs.component').then(
                (m) => m.FournisseursComponent,
              ),
          },
          {
            path: 'clients',
            canActivate: [permissionGuard],
            data: { permission: 'CLIENT_READ' },
            loadComponent: () =>
              import('./features/referentiel/clients/clients.component').then((m) => m.ClientsComponent),
          },
          {
            path: 'serveurs',
            loadComponent: () =>
              import('./features/referentiel/serveurs/serveurs.component').then((m) => m.ServeursComponent),
          },
        ],
      },
      {
        path: 'utilisateurs',
        canActivate: [permissionGuard],
        data: { permission: 'UTILISATEUR_READ' },
        loadComponent: () =>
          import('./features/utilisateurs/utilisateurs.component').then((m) => m.UtilisateursComponent),
      },
      {
        path: 'receptions',
        canActivate: [permissionGuard],
        data: { permission: 'RECEPTION_READ' },
        loadComponent: () =>
          import('./features/receptions/liste/receptions-liste.component').then(
            (m) => m.ReceptionsListeComponent,
          ),
      },
      {
        path: 'vente',
        canActivate: [permissionGuard],
        data: { permission: 'VENTE_WRITE' },
        loadComponent: () => import('./features/vente/vente.component').then((m) => m.VenteComponent),
      },
      {
        path: 'sessions-vente/:id',
        canActivate: [permissionGuard],
        data: { permission: 'SESSION_CLOTURER' },
        loadComponent: () =>
          import('./features/sessions-vente/session-detail.component').then((m) => m.SessionDetailComponent),
      },
      {
        path: 'receptions/:id',
        canActivate: [permissionGuard],
        data: { permission: 'RECEPTION_READ' },
        loadComponent: () =>
          import('./features/receptions/detail/reception-detail.component').then(
            (m) => m.ReceptionDetailComponent,
          ),
      },
    ],
  },
  { path: '**', redirectTo: 'tableau-de-bord' },
];
