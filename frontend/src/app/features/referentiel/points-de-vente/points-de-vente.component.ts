import { Component, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableModule } from '@angular/material/table';
import { form, FormField, required, schema, submit } from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { ReferentielApiService } from '../../../core/api/referentiel-api.service';
import { PointDeVenteResponse } from '../../../core/api/models/referentiel.model';
import { TypePointDeVente } from '../../../core/api/models/enums';
import { NotificationService } from '../../../core/ui/notification.service';
import { AuthService } from '../../../core/auth/auth.service';

interface PointDeVenteModele {
  libelle: string;
  type: TypePointDeVente;
  adresse: string;
}

const videCreation: PointDeVenteModele = { libelle: '', type: 'DEPOT', adresse: '' };

const pointDeVenteSchema = schema<PointDeVenteModele>((champ) => {
  required(champ.libelle, { message: 'Le libellé est requis.' });
  required(champ.type, { message: 'Le type est requis.' });
});

/** §5 — référentiel des points de vente (dépôt, bars/maquis). */
@Component({
  selector: 'app-points-de-vente',
  imports: [
    FormField,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatTableModule,
  ],
  templateUrl: './points-de-vente.component.html',
  styleUrl: '../referentiel-liste.scss',
})
export class PointsDeVenteComponent {
  private readonly api = inject(ReferentielApiService);
  private readonly notification = inject(NotificationService);
  protected readonly auth = inject(AuthService);

  protected readonly colonnes = ['libelle', 'type', 'adresse', 'actif', 'actions'];
  protected readonly enCours = signal(false);
  protected readonly idEnEdition = signal<number | null>(null);

  protected readonly modele = signal<PointDeVenteModele>({ ...videCreation });
  protected readonly formulaire = form(this.modele, pointDeVenteSchema);

  protected readonly pointsDeVente = rxResource({
    stream: () => this.api.pointsDeVente.lister({}, { size: 100, sort: 'libelle,asc' }),
  });

  protected async onSubmit(event: Event): Promise<void> {
    event.preventDefault();
    await submit(this.formulaire, async (f) => {
      this.enCours.set(true);
      try {
        const valeur = f().value();
        const id = this.idEnEdition();
        if (id === null) {
          await firstValueFrom(this.api.pointsDeVente.creer(valeur));
          this.notification.succes('Point de vente créé.');
        } else {
          const existant = this.pointsDeVente.value()?.content.find((x) => x.id === id);
          await firstValueFrom(
            this.api.pointsDeVente.modifier(id, {
              libelle: valeur.libelle,
              adresse: valeur.adresse,
              actif: existant?.actif ?? true,
            }),
          );
          this.notification.succes('Point de vente modifié.');
        }
        this.annulerEdition();
        this.pointsDeVente.reload();
        return undefined;
      } catch {
        return [{ fieldTree: this.formulaire, kind: 'server', message: 'Échec de l’enregistrement.' }];
      } finally {
        this.enCours.set(false);
      }
    });
  }

  protected modifier(pdv: PointDeVenteResponse): void {
    this.idEnEdition.set(pdv.id);
    this.modele.set({ libelle: pdv.libelle, type: pdv.type, adresse: pdv.adresse ?? '' });
  }

  protected annulerEdition(): void {
    this.idEnEdition.set(null);
    this.modele.set({ ...videCreation });
  }

  protected async basculerActif(pdv: PointDeVenteResponse): Promise<void> {
    await firstValueFrom(
      this.api.pointsDeVente.modifier(pdv.id, { libelle: pdv.libelle, adresse: pdv.adresse, actif: !pdv.actif }),
    );
    this.notification.succes(pdv.actif ? 'Point de vente désactivé.' : 'Point de vente activé.');
    this.pointsDeVente.reload();
  }
}
