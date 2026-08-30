import { DatePipe } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { rxResource, toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { firstValueFrom, map } from 'rxjs';
import { TransfertApiService } from '../../../core/api/transfert-api.service';
import { LIBELLES_STATUT_TRANSFERT } from '../../../core/api/models/enums';
import { AuthService } from '../../../core/auth/auth.service';
import { NotificationService } from '../../../core/ui/notification.service';
import { ConfirmService } from '../../../shared/components/confirm-dialog/confirm.service';
import { XofPipe } from '../../../shared/pipes/xof.pipe';

/** §9 — détail et cycle de vie d'un transfert dépôt → bar. */
@Component({
  selector: 'app-transfert-detail',
  imports: [DatePipe, MatButtonModule, MatCardModule, MatChipsModule, MatIconModule, MatProgressSpinnerModule, MatTableModule, XofPipe],
  templateUrl: './transfert-detail.component.html',
  styleUrl: './transfert-detail.component.scss',
})
export class TransfertDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(TransfertApiService);
  private readonly notification = inject(NotificationService);
  private readonly confirm = inject(ConfirmService);
  protected readonly auth = inject(AuthService);

  protected readonly libellesStatut: Record<string, string> = LIBELLES_STATUT_TRANSFERT;
  protected readonly colonnesLignes = ['produit', 'conditionnement', 'quantiteDemiCasiers', 'prixCessionCasierXof', 'montantLigneXof'];

  private readonly id = toSignal(this.route.paramMap.pipe(map((p) => Number(p.get('id')))), { initialValue: 0 });

  protected readonly transfert = rxResource({
    params: () => this.id(),
    stream: ({ params }) => this.api.obtenir(params),
  });

  protected readonly estClotureurCourant = computed(() => {
    const t = this.transfert.value();
    return t !== undefined && t.clotureePar === String(this.auth.utilisateurId());
  });

  protected async cloturer(): Promise<void> {
    const confirme = await this.confirm.demander({
      titre: 'Clôturer le transfert',
      message: 'Clôturer ce transfert ? Il passera en attente de validation.',
      libelleConfirmation: 'Clôturer',
    });
    if (!confirme) return;
    await firstValueFrom(this.api.cloturer(this.id()));
    this.notification.succes('Transfert clôturé, en attente de validation.');
    this.transfert.reload();
  }

  protected async valider(): Promise<void> {
    const confirme = await this.confirm.demander({
      titre: 'Valider le transfert',
      message:
        'Valider ce transfert ? Le stock source sera décrémenté et le stock destination incrémenté. Action irréversible.',
      libelleConfirmation: 'Valider',
    });
    if (!confirme) return;
    await firstValueFrom(this.api.valider(this.id()));
    this.notification.succes('Transfert validé, stocks mis à jour.');
    this.transfert.reload();
  }

  protected async annuler(): Promise<void> {
    const motif = window.prompt("Motif d'annulation :");
    if (!motif) return;
    await firstValueFrom(this.api.annuler(this.id(), { motif }));
    this.notification.succes('Transfert annulé.');
    this.transfert.reload();
  }

  protected retourListe(): void {
    this.router.navigateByUrl('/transferts');
  }
}
