import { inject } from '@angular/core';
import { CanActivateFn, CanMatchFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { Permission } from './auth.model';

/** Route protégée par authentification simple (§15.2). */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.estAuthentifie() || router.createUrlTree(['/connexion']);
};

/**
 * Route protégée par une permission précise. `data: { permission: 'X' }`
 * sur la route (voir app.routes.ts).
 */
export const permissionGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const permission = route.data['permission'] as Permission | undefined;
  if (!auth.estAuthentifie()) {
    return router.createUrlTree(['/connexion']);
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
