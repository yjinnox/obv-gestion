import { HttpContextToken } from '@angular/common/http';

/**
 * Désactive le toast d'erreur global de {@link errorInterceptor} sur une
 * requête donnée (`http.post(url, body, { context: new HttpContext().set(SANS_TOAST_ERREUR, true) })`) —
 * utile quand un formulaire affiche déjà l'erreur au plus près du champ
 * concerné plutôt qu'en toast générique.
 */
export const SANS_TOAST_ERREUR = new HttpContextToken<boolean>(() => false);
