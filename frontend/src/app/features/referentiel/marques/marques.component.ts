import { Component, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableModule } from '@angular/material/table';
import { form, FormField, required, schema, submit } from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { ReferentielApiService } from '../../../core/api/referentiel-api.service';
import { MarqueResponse } from '../../../core/api/models/referentiel.model';
import { NotificationService } from '../../../core/ui/notification.service';
import { AuthService } from '../../../core/auth/auth.service';

interface MarqueModele {
  libelle: string;
}

const marqueSchema = schema<MarqueModele>((champ) => {
  required(champ.libelle, { message: 'Le libellé est requis.' });
});

/** §5 — référentiel des marques (CRUD simple). */
@Component({
  selector: 'app-marques',
  imports: [
    FormField,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSlideToggleModule,
    MatTableModule,
  ],
  templateUrl: './marques.component.html',
  styleUrl: '../referentiel-liste.scss',
})
export class MarquesComponent {
  private readonly api = inject(ReferentielApiService);
  private readonly notification = inject(NotificationService);
  protected readonly auth = inject(AuthService);

  protected readonly colonnes = ['libelle', 'actif', 'actions'];
  protected readonly enCours = signal(false);
  protected readonly idEnEdition = signal<number | null>(null);

  protected readonly modele = signal<MarqueModele>({ libelle: '' });
  protected readonly formulaire = form(this.modele, marqueSchema);

  protected readonly marques = rxResource({
    stream: () => this.api.marques.lister({}, { size: 100, sort: 'libelle,asc' }),
  });

  protected async onSubmit(event: Event): Promise<void> {
    event.preventDefault();
    await submit(this.formulaire, async (f) => {
      this.enCours.set(true);
      try {
        const valeur = f().value();
        const id = this.idEnEdition();
        if (id === null) {
          await firstValueFrom(this.api.marques.creer(valeur));
          this.notification.succes('Marque créée.');
        } else {
          const existante = this.marques.value()?.content.find((m) => m.id === id);
          await firstValueFrom(this.api.marques.modifier(id, { ...valeur, actif: existante?.actif ?? true }));
          this.notification.succes('Marque modifiée.');
        }
        this.annulerEdition();
        this.marques.reload();
        return undefined;
      } catch {
        return [{ fieldTree: this.formulaire, kind: 'server', message: 'Échec de l’enregistrement.' }];
      } finally {
        this.enCours.set(false);
      }
    });
  }

  protected modifier(marque: MarqueResponse): void {
    this.idEnEdition.set(marque.id);
    this.modele.set({ libelle: marque.libelle });
  }

  protected annulerEdition(): void {
    this.idEnEdition.set(null);
    this.modele.set({ libelle: '' });
  }

  protected async basculerActif(marque: MarqueResponse): Promise<void> {
    await firstValueFrom(this.api.marques.modifier(marque.id, { libelle: marque.libelle, actif: !marque.actif }));
    this.notification.succes(marque.actif ? 'Marque désactivée.' : 'Marque activée.');
    this.marques.reload();
  }
}
