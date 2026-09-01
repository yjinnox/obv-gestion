import { DatePipe, KeyValuePipe } from '@angular/common';
import { HttpContext } from '@angular/common/http';
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
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { form, FormField, required, schema, submit } from '@angular/forms/signals';
import { firstValueFrom, map, of } from 'rxjs';
import { ReceptionApiService } from '../../../core/api/reception-api.service';
import { ReferentielApiService } from '../../../core/api/referentiel-api.service';
import { UtilisateurApiService } from '../../../core/api/utilisateur-api.service';
import { LigneReceptionResponse } from '../../../core/api/models/reception.model';
import { LIBELLES_STATUT_RECEPTION } from '../../../core/api/models/enums';
import { AuthService } from '../../../core/auth/auth.service';
import { NotificationService } from '../../../core/ui/notification.service';
import { ConfirmService } from '../../../shared/components/confirm-dialog/confirm.service';
import { XofPipe } from '../../../shared/pipes/xof.pipe';
import { SANS_TOAST_ERREUR } from '../../../core/http/http-context';

interface LigneModele {
  produitId: number | null;
  conditionnementId: number | null;
  nombreCasiers: number;
  prixAchatCasierXof: number | null;
}

const videLigne: LigneModele = {
  produitId: null,
  conditionnementId: null,
  nombreCasiers: 1,
  prixAchatCasierXof: null,
};

const ligneSchema = schema<LigneModele>((champ) => {
  required(champ.produitId, { message: 'Le produit est requis.' });
  required(champ.conditionnementId, { message: 'Le conditionnement est requis.' });
  required(champ.nombreCasiers, { message: 'Le nombre de casiers est requis.' });
});

/** §7 — détail et cycle de vie d'une réception (brouillon → validation). */
@Component({
  selector: 'app-reception-detail',
  imports: [
    DatePipe,
    KeyValuePipe,
    FormField,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatTableModule,
    XofPipe,
  ],
  templateUrl: './reception-detail.component.html',
  styleUrl: './reception-detail.component.scss',
})
export class ReceptionDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(ReceptionApiService);
  private readonly referentielApi = inject(ReferentielApiService);
  private readonly utilisateurApi = inject(UtilisateurApiService);
  private readonly notification = inject(NotificationService);
  private readonly confirm = inject(ConfirmService);
  protected readonly auth = inject(AuthService);

  protected readonly libellesStatut: Record<string, string> = LIBELLES_STATUT_RECEPTION;
  protected readonly colonnesLignes = ['produit', 'conditionnement', 'nombreCasiers', 'prixAchatCasierXof', 'montantLigneXof', 'actions'];

  private readonly id = toSignal(this.route.paramMap.pipe(map((p) => Number(p.get('id')))), { initialValue: 0 });

  protected readonly enCours = signal(false);
  protected readonly ligneEnEdition = signal<number | null>(null);
  protected readonly modeleLigne = signal<LigneModele>({ ...videLigne });
  protected readonly formulaireLigne = form(this.modeleLigne, ligneSchema);
  protected readonly destinataireManuel = signal<number | null>(null);
  protected readonly destinataireSelectionne = signal<number | null>(null);

  protected readonly reception = rxResource({
    params: () => this.id(),
    stream: ({ params }) => this.api.obtenir(params),
  });

  protected readonly recapitulatif = rxResource({
    params: () => (this.reception.value()?.statut === 'BROUILLON' ? null : this.id()),
    stream: ({ params }) => (params === null ? of(undefined) : this.api.recapitulatif(params)),
  });

  protected readonly produits = rxResource({
    stream: () => this.referentielApi.produits.lister({ actif: true }, { size: 200 }),
  });
  protected readonly conditionnements = rxResource({
    stream: () => this.referentielApi.conditionnements.lister({ actif: true }, { size: 200 }),
  });
  protected readonly conditionnementsDuProduit = computed(() => {
    const produitId = this.modeleLigne().produitId;
    return this.conditionnements.value()?.content.filter((c) => c.produitId === produitId) ?? [];
  });

  // §4.3 — RECEPTION_WRITE (GERANT_DEPOT) n'a pas UTILISATEUR_READ : cette
  // liste échoue silencieusement (403) pour ce rôle, qui utilise alors le
  // champ de secours `destinataireManuel` (RG-01, séparation des tâches).
  protected readonly destinataires = rxResource({
    stream: () =>
      this.utilisateurApi.lister(
        { role: 'SUPER_ADMINISTRATEUR', statut: 'ACTIF' },
        { size: 50 },
        new HttpContext().set(SANS_TOAST_ERREUR, true),
      ),
  });

  /**
   * S'envoyer à soi-même une demande de validation n'a aucun sens : on
   * exclut donc l'utilisateur courant de la liste. Un SUPER_ADMINISTRATEUR
   * qui a clôturé valide directement (bouton « Valider »), la demande ne
   * sert qu'à solliciter un autre administrateur.
   */
  protected readonly destinatairesPossibles = computed(() =>
    (this.destinataires.value()?.content ?? []).filter((u) => u.id !== this.auth.utilisateurId()),
  );

  protected async onSubmitLigne(event: Event): Promise<void> {
    event.preventDefault();
    await submit(this.formulaireLigne, async (f) => {
      this.enCours.set(true);
      try {
        const valeur = f().value();
        if (valeur.produitId === null || valeur.conditionnementId === null) {
          return undefined;
        }
        const ligneId = this.ligneEnEdition();
        if (ligneId === null) {
          await firstValueFrom(
            this.api.ajouterLigne(this.id(), {
              produitId: valeur.produitId,
              conditionnementId: valeur.conditionnementId,
              nombreCasiers: valeur.nombreCasiers,
              prixAchatCasierXof: valeur.prixAchatCasierXof,
            }),
          );
          this.notification.succes('Ligne ajoutée.');
        } else {
          await firstValueFrom(
            this.api.modifierLigne(this.id(), ligneId, {
              nombreCasiers: valeur.nombreCasiers,
              prixAchatCasierXof: valeur.prixAchatCasierXof ?? 0,
            }),
          );
          this.notification.succes('Ligne modifiée.');
        }
        this.annulerEditionLigne();
        this.reception.reload();
        return undefined;
      } catch {
        return [{ fieldTree: this.formulaireLigne, kind: 'server', message: 'Échec de l’enregistrement.' }];
      } finally {
        this.enCours.set(false);
      }
    });
  }

  protected modifierLigne(ligne: LigneReceptionResponse): void {
    this.ligneEnEdition.set(ligne.id);
    this.modeleLigne.set({
      produitId: ligne.produitId,
      conditionnementId: ligne.conditionnementId,
      nombreCasiers: ligne.nombreCasiers,
      prixAchatCasierXof: ligne.prixAchatCasierXof,
    });
  }

  protected annulerEditionLigne(): void {
    this.ligneEnEdition.set(null);
    this.modeleLigne.set({ ...videLigne });
  }

  protected async supprimerLigne(ligneId: number): Promise<void> {
    const confirme = await this.confirm.demander({
      titre: 'Supprimer la ligne',
      message: 'Supprimer cette ligne de la réception ?',
      destructif: true,
      libelleConfirmation: 'Supprimer',
    });
    if (!confirme) return;
    await firstValueFrom(this.api.supprimerLigne(this.id(), ligneId));
    this.notification.succes('Ligne supprimée.');
    this.reception.reload();
  }

  protected async cloturer(): Promise<void> {
    const confirme = await this.confirm.demander({
      titre: 'Clôturer la réception',
      message: 'Clôturer la réception ? Les lignes ne pourront plus être modifiées après validation.',
      libelleConfirmation: 'Clôturer',
    });
    if (!confirme) return;
    await firstValueFrom(this.api.cloturer(this.id()));
    this.notification.succes('Réception clôturée, en attente de validation.');
    this.reception.reload();
  }

  protected async demanderValidation(): Promise<void> {
    const destinataireId = this.destinataireSelectionne() ?? this.destinataireManuel();
    if (destinataireId === null) {
      this.notification.erreur('Sélectionnez ou saisissez un destinataire.');
      return;
    }
    await firstValueFrom(this.api.demanderValidation(this.id(), { destinataireId }));
    this.notification.succes('Demande de validation envoyée.');
    this.reception.reload();
  }

  protected async valider(): Promise<void> {
    const confirme = await this.confirm.demander({
      titre: 'Valider la réception',
      message:
        'Valider définitivement cette réception ? Le stock a déjà été incrémenté à la clôture ; après validation, '
        + 'la réception ne pourra plus être ni corrigée ni annulée.',
      libelleConfirmation: 'Valider',
    });
    if (!confirme) return;
    await firstValueFrom(this.api.valider(this.id()));
    this.notification.succes('Réception validée, stock mis à jour.');
    this.reception.reload();
  }

  protected async annuler(): Promise<void> {
    const motif = window.prompt("Motif d'annulation :");
    if (!motif) return;
    await firstValueFrom(this.api.annuler(this.id(), { motif }));
    this.notification.succes('Réception annulée.');
    this.reception.reload();
  }

  protected retourListe(): void {
    this.router.navigateByUrl('/receptions');
  }
}
