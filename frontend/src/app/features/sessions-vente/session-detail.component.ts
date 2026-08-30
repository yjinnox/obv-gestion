import { DatePipe, KeyValuePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { rxResource, toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { firstValueFrom, map } from 'rxjs';
import { SessionVenteApiService } from '../../core/api/session-vente-api.service';
import { LIBELLES_STATUT_SESSION_VENTE } from '../../core/api/models/enums';
import { AuthService } from '../../core/auth/auth.service';
import { NotificationService } from '../../core/ui/notification.service';
import { ConfirmService } from '../../shared/components/confirm-dialog/confirm.service';
import { XofPipe } from '../../shared/pipes/xof.pipe';

/** §8.1, §8.3 — récapitulatif et cycle de vie d'une session de vente. */
@Component({
  selector: 'app-session-detail',
  imports: [
    DatePipe,
    KeyValuePipe,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
    XofPipe,
  ],
  templateUrl: './session-detail.component.html',
  styleUrl: './session-detail.component.scss',
})
export class SessionDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(SessionVenteApiService);
  private readonly notification = inject(NotificationService);
  private readonly confirm = inject(ConfirmService);
  protected readonly auth = inject(AuthService);

  protected readonly libellesStatut: Record<string, string> = LIBELLES_STATUT_SESSION_VENTE;
  private readonly id = toSignal(this.route.paramMap.pipe(map((p) => Number(p.get('id')))), { initialValue: 0 });

  protected readonly enCours = signal(false);
  protected readonly totalCompte = signal<number>(0);

  protected readonly recapitulatif = rxResource({
    params: () => this.id(),
    stream: ({ params }) => this.api.recapitulatif(params),
  });

  protected readonly estClotureurCourant = computed(() => {
    const session = this.recapitulatif.value()?.session;
    return session !== undefined && session.clotureePar === String(this.auth.utilisateurId());
  });

  protected async cloturer(): Promise<void> {
    const confirme = await this.confirm.demander({
      titre: 'Clôturer la session',
      message: 'Clôturer la session de vente avec le montant compté en caisse ?',
      libelleConfirmation: 'Clôturer',
    });
    if (!confirme) return;
    await firstValueFrom(this.api.cloturer(this.id(), { totalCompteXof: this.totalCompte() }));
    this.notification.succes('Session clôturée.');
    this.recapitulatif.reload();
  }

  protected async valider(): Promise<void> {
    const confirme = await this.confirm.demander({
      titre: 'Valider la session',
      message: 'Valider définitivement cette session de vente ? Cette action est irréversible.',
      libelleConfirmation: 'Valider',
    });
    if (!confirme) return;
    await firstValueFrom(this.api.valider(this.id()));
    this.notification.succes('Session validée.');
    this.recapitulatif.reload();
  }

  protected async demanderModification(): Promise<void> {
    await firstValueFrom(this.api.demanderModification(this.id()));
    this.notification.succes('Session réouverte pour modification.');
    this.recapitulatif.reload();
  }

  protected retour(): void {
    this.router.navigateByUrl('/vente');
  }
}
