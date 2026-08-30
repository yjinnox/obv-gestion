import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../ui/notification.service';
import { estProblemDetail } from './problem-detail';
import { SANS_TOAST_ERREUR } from './http-context';

/**
 * Toast d'erreur générique (§15.3, feedback immédiat) sur toute requête API
 * en échec, sauf opt-out explicite via {@link SANS_TOAST_ERREUR} (formulaires
 * qui affichent déjà l'erreur au plus près du champ).
 */
export const errorInterceptor: HttpInterceptorFn = (requete, next) => {
  const notification = inject(NotificationService);

  return next(requete).pipe(
    catchError((erreur: unknown) => {
      if (requete.context.get(SANS_TOAST_ERREUR)) {
        return throwError(() => erreur);
      }
      if (erreur instanceof HttpErrorResponse && erreur.status === 401) {
        // Géré par authInterceptor (rafraîchissement) ou redirection vers /connexion.
        return throwError(() => erreur);
      }
      if (erreur instanceof HttpErrorResponse) {
        const message = estProblemDetail(erreur.error) && erreur.error.detail
          ? erreur.error.detail
          : 'Une erreur de communication avec le serveur est survenue.';
        notification.erreur(message);
      }
      return throwError(() => erreur);
    }),
  );
};
