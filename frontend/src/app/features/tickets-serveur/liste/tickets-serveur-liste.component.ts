import { HttpContext, HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { form, FormField, required, schema, submit } from '@angular/forms/signals';
import { catchError, firstValueFrom, of, throwError } from 'rxjs';
import { TicketServeurApiService } from '../../../core/api/ticket-serveur-api.service';
import { ReferentielApiService } from '../../../core/api/referentiel-api.service';
import { SessionVenteApiService } from '../../../core/api/session-vente-api.service';
import { LIBELLES_STATUT_TICKET } from '../../../core/api/models/enums';
import { SANS_TOAST_ERREUR } from '../../../core/http/http-context';
import { NotificationService } from '../../../core/ui/notification.service';
import { XofPipe } from '../../../shared/pipes/xof.pipe';

interface OuvertureModele {
  pointDeVenteId: number | null;
  fondCaisseXof: number;
}

const ouvertureSchema = schema<OuvertureModele>((champ) => {
  required(champ.pointDeVenteId, { message: 'Le point de vente est requis.' });
  required(champ.fondCaisseXof, { message: 'Le fond de caisse est requis.' });
});

/** §10, RG-33 — vente bar : tickets ouverts par serveur pour la session en cours. */
@Component({
  selector: 'app-tickets-serveur-liste',
  imports: [
    RouterLink,
    FormField,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    XofPipe,
  ],
  templateUrl: './tickets-serveur-liste.component.html',
  styleUrl: './tickets-serveur-liste.component.scss',
})
export class TicketsServeurListeComponent {
  private readonly ticketApi = inject(TicketServeurApiService);
  private readonly referentielApi = inject(ReferentielApiService);
  private readonly sessionApi = inject(SessionVenteApiService);
  private readonly notification = inject(NotificationService);
  private readonly router = inject(Router);

  protected readonly libellesStatut: Record<string, string> = LIBELLES_STATUT_TICKET;
  protected readonly enCours = signal(false);
  protected readonly pdvSelectionne = signal<number | null>(null);
  protected readonly serveurAOuvrir = signal<number | null>(null);

  protected readonly pointsDeVente = rxResource({
    stream: () => this.referentielApi.pointsDeVente.lister({ actif: true }, { size: 100 }),
  });
  protected readonly pointsDeVenteBar = computed(
    () => this.pointsDeVente.value()?.content.filter((p) => p.type === 'BAR') ?? [],
  );

  protected readonly modeleOuverture = signal<OuvertureModele>({ pointDeVenteId: null, fondCaisseXof: 0 });
  protected readonly formulaireOuverture = form(this.modeleOuverture, ouvertureSchema);

  protected readonly sessionCourante = rxResource({
    params: () => this.pdvSelectionne(),
    stream: ({ params }) =>
      params === null
        ? of(undefined)
        : this.sessionApi.courante(params, new HttpContext().set(SANS_TOAST_ERREUR, true)).pipe(
            catchError((erreur: unknown) =>
              erreur instanceof HttpErrorResponse && erreur.status === 404 ? of(undefined) : throwError(() => erreur),
            ),
          ),
  });

  protected readonly serveurs = rxResource({
    params: () => this.pdvSelectionne(),
    stream: ({ params }) =>
      params === null ? of(undefined) : this.referentielApi.serveurs.lister({ pdv: params, actif: true }, { size: 100 }),
  });

  protected readonly tickets = rxResource({
    params: () => this.sessionCourante.value()?.id ?? null,
    stream: ({ params }) =>
      params === null ? of(undefined) : this.ticketApi.lister({ sessionVenteId: params }, { size: 100 }),
  });

  protected async onSubmitOuverture(event: Event): Promise<void> {
    event.preventDefault();
    await submit(this.formulaireOuverture, async (f) => {
      this.enCours.set(true);
      try {
        const valeur = f().value();
        if (valeur.pointDeVenteId === null) return undefined;
        await firstValueFrom(
          this.sessionApi.ouvrir({ pointDeVenteId: valeur.pointDeVenteId, fondCaisseXof: valeur.fondCaisseXof }),
        );
        this.notification.succes('Session ouverte.');
        this.sessionCourante.reload();
        return undefined;
      } catch {
        return [{ fieldTree: this.formulaireOuverture, kind: 'server', message: "Échec de l'ouverture." }];
      } finally {
        this.enCours.set(false);
      }
    });
  }

  protected async ouvrirNouveauTicket(): Promise<void> {
    const session = this.sessionCourante.value();
    if (!session || this.serveurAOuvrir() === null) return;
    const ticket = await firstValueFrom(
      this.ticketApi.creer({ sessionVenteId: session.id, serveurId: this.serveurAOuvrir()! }),
    );
    await this.router.navigate(['/tickets-serveur', ticket.id]);
  }
}
