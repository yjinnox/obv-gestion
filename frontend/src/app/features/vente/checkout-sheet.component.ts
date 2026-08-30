import { Component, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MAT_BOTTOM_SHEET_DATA, MatBottomSheetRef } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { firstValueFrom } from 'rxjs';
import { VenteApiService } from '../../core/api/vente-api.service';
import { ReferentielApiService } from '../../core/api/referentiel-api.service';
import { PanierResponse } from '../../core/api/models/vente.model';
import { SessionVenteResponse, VenteResponse } from '../../core/api/models/vente.model';
import { ModePaiement } from '../../core/api/models/enums';
import { NotificationService } from '../../core/ui/notification.service';
import { XofPipe } from '../../shared/pipes/xof.pipe';
import { telechargerFichier } from '../../shared/util/telecharger-fichier';

// RG-07 — VenteService rejette une commande sans client (existant ou
// nouveau) : contrairement à la vente bar (ticket serveur, anonyme), la
// vente dépôt est toujours associée à un client.
type ModeClient = 'existant' | 'nouveau';

/** §8.2, §15.3 — panier + finalisation de commande (feuille en pied d'écran). */
@Component({
  selector: 'app-checkout-sheet',
  imports: [
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    XofPipe,
  ],
  templateUrl: './checkout-sheet.component.html',
  styleUrl: './checkout-sheet.component.scss',
})
export class CheckoutSheetComponent {
  private readonly data = inject<{ session: SessionVenteResponse; panier: PanierResponse }>(MAT_BOTTOM_SHEET_DATA);
  private readonly ref = inject(MatBottomSheetRef<CheckoutSheetComponent>);
  private readonly venteApi = inject(VenteApiService);
  private readonly referentielApi = inject(ReferentielApiService);
  private readonly notification = inject(NotificationService);

  protected readonly session = this.data.session;
  protected readonly panier = this.data.panier;

  protected readonly enCours = signal(false);
  protected readonly venteCreee = signal<VenteResponse | null>(null);

  protected readonly modeClient = signal<ModeClient>('existant');
  protected readonly clientSelectionne = signal<number | null>(null);
  protected readonly nouveauNom = signal('');
  protected readonly nouveauPrenoms = signal('');
  protected readonly nouveauTelephone = signal('');
  protected readonly modePaiement = signal<ModePaiement>('ESPECES');

  protected readonly clients = rxResource({
    stream: () => this.referentielApi.clients.lister({ actif: true }, { size: 200 }),
  });

  protected async commander(): Promise<void> {
    if (this.modeClient() === 'nouveau' && !this.nouveauTelephone()) {
      this.notification.erreur('Le téléphone du nouveau client est requis.');
      return;
    }
    if (this.modeClient() === 'existant' && this.clientSelectionne() === null) {
      this.notification.erreur('Sélectionnez un client.');
      return;
    }
    this.enCours.set(true);
    try {
      const vente = await firstValueFrom(
        this.venteApi.commander({
          sessionVenteId: this.session.id,
          clientId: this.modeClient() === 'existant' ? this.clientSelectionne() : null,
          nouveauClient:
            this.modeClient() === 'nouveau'
              ? {
                  type: 'PARTICULIER',
                  nom: this.nouveauNom(),
                  prenoms: this.nouveauPrenoms(),
                  telephone: this.nouveauTelephone(),
                }
              : null,
          modePaiement: this.modePaiement(),
        }),
      );
      this.venteCreee.set(vente);
      this.notification.succes(`Commande ${vente.numeroBonCommande} enregistrée.`);
    } finally {
      this.enCours.set(false);
    }
  }

  protected async telechargerBonDeCommande(): Promise<void> {
    const vente = this.venteCreee();
    if (!vente) return;
    const blob = await firstValueFrom(this.venteApi.telechargerBonDeCommande(vente.id));
    telechargerFichier(blob, `${vente.numeroBonCommande}.pdf`);
  }

  protected async telechargerFacture(): Promise<void> {
    const vente = this.venteCreee();
    if (!vente) return;
    const blob = await firstValueFrom(this.venteApi.telechargerFacture(vente.id));
    telechargerFichier(blob, `${vente.numeroFacture}.pdf`);
  }

  protected fermer(): void {
    this.ref.dismiss(this.venteCreee() !== null);
  }
}
