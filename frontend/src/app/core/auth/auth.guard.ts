import { inject } from '@angular/core';
import { CanActivateFn, CanMatchFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { Permission } from './auth.model';

/**
 * Redirige vers la connexion en mémorisant l'URL demandée, pour y revenir
 * une fois authentifié : les liens profonds envoyés par email (demande de
 * validation d'une réception, §7.2) doivent aboutir sur la page visée, pas
 * sur le tableau de bord.
 */
function versConnexion(router: Router, urlDemandee: string) {
  return router.createUrlTree(['/connexion'], { queryParams: { returnUrl: urlDemandee } });
}

/** Route protégée par authentification simple (§15.2). */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.estAuthentifie() || versConnexion(router, state.url);
};

/**
 * Route protégée par une permission précise. `data: { permission: 'X' }`
 * sur la route (voir app.routes.ts).
 */
export const permissionGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const permission = route.data['permission'] as Permission | undefined;
  if (!auth.estAuthentifie()) {
    return versConnexion(router, state.url);
  }
  if (permission && !auth.aLaPermission(permission)) {
    return router.createUrlTree(['/tableau-de-bord']);
  }
  return true;
};

/**
 * Empêche l'accès aux écrans de connexion/activation une fois authentifié.
 *
 * Renvoie `false` (jamais une {@link UrlTree}) : un `canMatch` qui redirige
 * vers une URL qui repasse par ce même garde boucle indéfiniment (la
 * redirection déclenche une nouvelle navigation, qui réévalue ce garde,
 * qui redirige à nouveau, ...). `false` fait simplement échouer ce
 * candidat de route pour que le routeur essaie la configuration suivante
 * (`path: ''` avec {@link MainLayoutComponent} dans app.routes.ts).
 */
export const invitesGuard: CanMatchFn = () => {
  const auth = inject(AuthService);
  return !auth.estAuthentifie();
};
