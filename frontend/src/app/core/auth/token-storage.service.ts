import { Injectable } from '@angular/core';
import { SessionPersistee } from './auth.model';

const CLE_STOCKAGE = 'obv-gestion.session';

/**
 * Persistance de la session en `localStorage` (survit au rechargement de
 * page). Isolée dans un service dédié pour rester la seule dépendance au
 * stockage du navigateur (facilite un remplacement futur, ex. cookie
 * httpOnly).
 */
@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  charger(): SessionPersistee | null {
    const brut = localStorage.getItem(CLE_STOCKAGE);
    if (!brut) {
      return null;
    }
    try {
      return JSON.parse(brut) as SessionPersistee;
    } catch {
      localStorage.removeItem(CLE_STOCKAGE);
      return null;
    }
  }

  enregistrer(session: SessionPersistee): void {
    localStorage.setItem(CLE_STOCKAGE, JSON.stringify(session));
  }

  effacer(): void {
    localStorage.removeItem(CLE_STOCKAGE);
  }
}
