import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

export interface ConfirmDialogData {
  titre: string;
  message: string;
  /** Libellé du bouton de confirmation (défaut « Confirmer »). */
  libelleConfirmation?: string;
  /** Style destructif (rouge) pour annulation/suppression (§15.4). */
  destructif?: boolean;
}

/** Boîte de confirmation générique avant une action irréversible. */
@Component({
  selector: 'app-confirm-dialog',
  imports: [MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>{{ data.titre }}</h2>
    <mat-dialog-content>{{ data.message }}</mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="dialogRef.close(false)">Annuler</button>
      <button
        mat-flat-button
        [color]="data.destructif ? 'warn' : 'primary'"
        (click)="dialogRef.close(true)"
      >
        {{ data.libelleConfirmation ?? 'Confirmer' }}
      </button>
    </mat-dialog-actions>
  `,
})
export class ConfirmDialogComponent {
  readonly dialogRef = inject(MatDialogRef<ConfirmDialogComponent>);
  readonly data = inject<ConfirmDialogData>(MAT_DIALOG_DATA);
}
