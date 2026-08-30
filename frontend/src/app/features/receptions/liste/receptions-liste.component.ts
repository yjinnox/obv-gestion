import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { form, FormField, required, schema, submit } from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { ReceptionApiService } from '../../../core/api/reception-api.service';
import { ReferentielApiService } from '../../../core/api/referentiel-api.service';
import { LIBELLES_STATUT_RECEPTION, StatutReception } from '../../../core/api/models/enums';
import { AuthService } from '../../../core/auth/auth.service';
import { XofPipe } from '../../../shared/pipes/xof.pipe';

interface NouvelleReceptionModele {
  fournisseurId: number | null;
  pointDeVenteId: number | null;
  dateHeureLivraison: string;
}

function maintenantLocal(): string {
  const d = new Date();
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 16);
}

const videCreation: NouvelleReceptionModele = {
  fournisseurId: null,
  pointDeVenteId: null,
  dateHeureLivraison: maintenantLocal(),
};

const receptionSchema = schema<NouvelleReceptionModele>((champ) => {
  required(champ.fournisseurId, { message: 'Le fournisseur est requis.' });
  required(champ.pointDeVenteId, { message: 'Le point de vente est requis.' });
  required(champ.dateHeureLivraison, { message: 'La date de livraison est requise.' });
});

/** §7 — liste des réceptions et création d'une nouvelle réception (BROUILLON). */
@Component({
  selector: 'app-receptions-liste',
  imports: [
    DatePipe,
    FormField,
    MatButtonModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatTableModule,
    XofPipe,
  ],
  templateUrl: './receptions-liste.component.html',
  styleUrl: './receptions-liste.component.scss',
})
export class ReceptionsListeComponent {
  private readonly api = inject(ReceptionApiService);
  private readonly referentielApi = inject(ReferentielApiService);
  private readonly router = inject(Router);
  protected readonly auth = inject(AuthService);

  protected readonly libellesStatut: Record<string, string> = LIBELLES_STATUT_RECEPTION;
  protected readonly colonnes = ['fournisseur', 'pointDeVente', 'dateHeureLivraison', 'statut', 'montantTotalXof'];

  protected readonly afficherCreation = signal(false);
  protected readonly enCours = signal(false);
  protected readonly filtreStatut = signal<StatutReception | null>(null);

  protected readonly modele = signal<NouvelleReceptionModele>({ ...videCreation });
  protected readonly formulaire = form(this.modele, receptionSchema);

  protected readonly fournisseurs = rxResource({
    stream: () => this.referentielApi.fournisseurs.lister({ actif: true }, { size: 100 }),
  });
  private readonly tousLesPointsDeVente = rxResource({
    stream: () => this.referentielApi.pointsDeVente.lister({ actif: true }, { size: 100 }),
  });
  protected readonly pointsDeVenteDepot = computed(
    () => this.tousLesPointsDeVente.value()?.content.filter((p) => p.type === 'DEPOT') ?? [],
  );

  protected readonly receptions = rxResource({
    params: () => this.filtreStatut(),
    stream: ({ params }) =>
      this.api.lister({ statut: params ?? undefined }, { size: 50, sort: 'dateHeureLivraison,desc' }),
  });

  protected async onSubmit(event: Event): Promise<void> {
    event.preventDefault();
    await submit(this.formulaire, async (f) => {
      this.enCours.set(true);
      try {
        const valeur = f().value();
        if (valeur.fournisseurId === null || valeur.pointDeVenteId === null) {
          return undefined;
        }
        const reception = await firstValueFrom(
          this.api.creer({
            fournisseurId: valeur.fournisseurId,
            pointDeVenteId: valeur.pointDeVenteId,
            dateHeureLivraison: new Date(valeur.dateHeureLivraison).toISOString(),
          }),
        );
        await this.router.navigate(['/receptions', reception.id]);
        return undefined;
      } catch {
        return [{ fieldTree: this.formulaire, kind: 'server', message: 'Échec de la création.' }];
      } finally {
        this.enCours.set(false);
      }
    });
  }

  protected ouvrir(id: number): void {
    this.router.navigate(['/receptions', id]);
  }
}
