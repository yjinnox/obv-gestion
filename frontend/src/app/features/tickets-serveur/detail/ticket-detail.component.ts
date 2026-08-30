import { Component, computed, inject, signal } from '@angular/core';
import { rxResource, toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { firstValueFrom, map, of } from 'rxjs';
import { TicketServeurApiService } from '../../../core/api/ticket-serveur-api.service';
import { ReferentielApiService } from '../../../core/api/referentiel-api.service';
import { SessionVenteApiService } from '../../../core/api/session-vente-api.service';
import { ModePaiement } from '../../../core/api/models/enums';
import { LIBELLES_STATUT_TICKET } from '../../../core/api/models/enums';
import { ProduitResponse } from '../../../core/api/models/referentiel.model';
import { AuthService } from '../../../core/auth/auth.service';
import { NotificationService } from '../../../core/ui/notification.service';
import { ConfirmService } from '../../../shared/components/confirm-dialog/confirm.service';
import { XofPipe } from '../../../shared/pipes/xof.pipe';

/** §10, RG-34 — ticket serveur : ajout de lignes en bouteilles puis encaissement. */
@Component({
  selector: 'app-ticket-detail',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatTableModule,
    XofPipe,
  ],
  templateUrl: './ticket-detail.component.html',
  styleUrl: './ticket-detail.component.scss',
})
export class TicketDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(TicketServeurApiService);
  private readonly referentielApi = inject(ReferentielApiService);
  private readonly sessionApi = inject(SessionVenteApiService);
  private readonly notification = inject(NotificationService);
  private readonly confirm = inject(ConfirmService);
  protected readonly auth = inject(AuthService);

  protected readonly libellesStatut: Record<string, string> = LIBELLES_STATUT_TICKET;
  protected readonly colonnesLignes = ['produit', 'quantiteBouteilles', 'prixVenteBouteilleXof', 'montantLigneXof'];

  private readonly id = toSignal(this.route.paramMap.pipe(map((p) => Number(p.get('id')))), { initialValue: 0 });

  protected readonly modePaiement = signal<ModePaiement>('ESPECES');

  protected readonly ticket = rxResource({
    params: () => this.id(),
    stream: ({ params }) => this.api.obtenir(params),
  });

  protected readonly produits = rxResource({
    stream: () => this.referentielApi.produits.lister({ actif: true }, { size: 200 }),
  });

  // TicketServeurResponse n'expose pas directement le point de vente : il
  // faut passer par la session de vente pour connaître le bar concerné et
  // filtrer les tarifs BOUTEILLE au bon point de vente.
  protected readonly session = rxResource({
    params: () => this.ticket.value()?.sessionVenteId ?? null,
    stream: ({ params }) => (params === null ? of(undefined) : this.sessionApi.obtenir(params)),
  });

  protected readonly tarifsBouteille = rxResource({
    params: () => this.session.value()?.pointDeVenteId ?? null,
    stream: ({ params }) =>
      params === null ? of(undefined) : this.referentielApi.listerTarifs({ pdv: params, nature: 'VENTE' }, { size: 200 }),
  });

  protected readonly produitsVendables = computed(() => {
    const tarifs = (this.tarifsBouteille.value()?.content ?? []).filter((t) => !t.dateFin && t.uniteVente === 'BOUTEILLE');
    const parProduit = new Map(tarifs.map((t) => [t.produitId, t.montantXof]));
    return (this.produits.value()?.content ?? [])
      .filter((p) => parProduit.has(p.id))
      .map((p) => ({ produit: p, prixXof: parProduit.get(p.id)! }));
  });

  protected async ajouterBouteille(produit: ProduitResponse): Promise<void> {
    await firstValueFrom(this.api.ajouterLigne(this.id(), { produitId: produit.id, quantiteBouteilles: 1 }));
    this.ticket.reload();
  }

  protected async encaisser(): Promise<void> {
    const confirme = await this.confirm.demander({
      titre: 'Encaisser le ticket',
      message: 'Encaisser ce ticket ? Le stock de bouteilles sera décrémenté. Action irréversible.',
      libelleConfirmation: 'Encaisser',
    });
    if (!confirme) return;
    await firstValueFrom(this.api.encaisser(this.id(), { modePaiement: this.modePaiement() }));
    this.notification.succes('Ticket encaissé.');
    this.ticket.reload();
  }

  protected retourListe(): void {
    this.router.navigateByUrl('/tickets-serveur');
  }
}
