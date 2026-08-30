import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

/** Retours immédiats (succès/erreur) en français (§15.3). */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly snackBar = inject(MatSnackBar);

  succes(message: string): void {
    this.snackBar.open(message, 'Fermer', { duration: 4000, panelClass: 'notification-succes' });
  }

  erreur(message: string): void {
    this.snackBar.open(message, 'Fermer', { duration: 6000, panelClass: 'notification-erreur' });
  }
}
