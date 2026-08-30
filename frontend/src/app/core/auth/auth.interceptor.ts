import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, Observable, share, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { ConnexionResponse } from './auth.model';

const PREFIXE_AUTH = '/api/v1/auth';

// Partagé entre toutes les requêtes en échec simultanément (§15.2, refresh
// automatique) : évite un rafraîchissement en rafale si plusieurs appels
// API échouent au même instant avec le même jeton expiré.
let rafraichissementEnCours: Observable<ConnexionResponse> | null = null;

/**
 * Ajoute le jeton d'accès sur les requêtes API et tente un rafraîchissement
 * automatique unique sur un 401 (§15.2). Les routes `/api/v1/auth/**`
 * (login, refresh, activation) ne portent jamais de jeton : elles sont
 * publiques côté API.
 */
export const authInterceptor: HttpInterceptorFn = (requete, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const estRequeteAuth = requete.url.startsWith(PREFIXE_AUTH);
  const jeton = auth.accessToken();
  const requeteAuthentifiee =
    !estRequeteAuth && jeton ? requete.clone({ setHeaders: { Authorization: `Bearer ${jeton}` } }) : requete;

  return next(requeteAuthentifiee).pipe(
    catchError((erreur: unknown) => {
      if (!(erreur instanceof HttpErrorResponse) || erreur.status !== 401 || estRequeteAuth) {
        return throwError(() => erreur);
      }
      if (!auth.refreshToken()) {
        router.navigateByUrl('/connexion');
        return throwError(() => erreur);
      }

      rafraichissementEnCours ??= auth.rafraichir().pipe(share());

      return rafraichissementEnCours.pipe(
        switchMap((reponse) => {
          rafraichissementEnCours = null;
          const requeteRejouee = requete.clone({
            setHeaders: { Authorization: `Bearer ${reponse.accessToken}` },
          });
          return next(requeteRejouee);
        }),
        catchError((erreurRefresh: unknown) => {
          rafraichissementEnCours = null;
          auth.deconnecter();
          router.navigateByUrl('/connexion');
          return throwError(() => erreurRefresh);
        }),
      );
    }),
  );
};
