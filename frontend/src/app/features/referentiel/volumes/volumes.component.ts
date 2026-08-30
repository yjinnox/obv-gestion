import { Component, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableModule } from '@angular/material/table';
import { form, FormField, min, required, schema, submit } from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { ReferentielApiService } from '../../../core/api/referentiel-api.service';
import { VolumeResponse } from '../../../core/api/models/referentiel.model';
import { NotificationService } from '../../../core/ui/notification.service';
import { AuthService } from '../../../core/auth/auth.service';

interface VolumeModele {
  libelle: string;
  contenanceMl: number;
}

const volumeSchema = schema<VolumeModele>((champ) => {
  required(champ.libelle, { message: 'Le libellé est requis.' });
  required(champ.contenanceMl, { message: 'La contenance est requise.' });
  min(champ.contenanceMl, 1, { message: 'La contenance doit être positive.' });
});

/** §5 — référentiel des volumes (33 cl, 65 cl, ...). */
@Component({
  selector: 'app-volumes',
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
  templateUrl: './volumes.component.html',
  styleUrl: '../referentiel-liste.scss',
})
export class VolumesComponent {
  private readonly api = inject(ReferentielApiService);
  private readonly notification = inject(NotificationService);
  protected readonly auth = inject(AuthService);

  protected readonly colonnes = ['libelle', 'contenanceMl', 'actif', 'actions'];
  protected readonly enCours = signal(false);
  protected readonly idEnEdition = signal<number | null>(null);

  protected readonly modele = signal<VolumeModele>({ libelle: '', contenanceMl: 0 });
  protected readonly formulaire = form(this.modele, volumeSchema);

  protected readonly volumes = rxResource({
    stream: () => this.api.volumes.lister({}, { size: 100, sort: 'contenanceMl,asc' }),
  });

  protected async onSubmit(event: Event): Promise<void> {
    event.preventDefault();
    await submit(this.formulaire, async (f) => {
      this.enCours.set(true);
      try {
        const valeur = f().value();
        const id = this.idEnEdition();
        if (id === null) {
          await firstValueFrom(this.api.volumes.creer(valeur));
          this.notification.succes('Volume créé.');
        } else {
          const existant = this.volumes.value()?.content.find((v) => v.id === id);
          await firstValueFrom(this.api.volumes.modifier(id, { ...valeur, actif: existant?.actif ?? true }));
          this.notification.succes('Volume modifié.');
        }
        this.annulerEdition();
        this.volumes.reload();
        return undefined;
      } catch {
        return [{ fieldTree: this.formulaire, kind: 'server', message: 'Échec de l’enregistrement.' }];
      } finally {
        this.enCours.set(false);
      }
    });
  }

  protected modifier(volume: VolumeResponse): void {
    this.idEnEdition.set(volume.id);
    this.modele.set({ libelle: volume.libelle, contenanceMl: volume.contenanceMl });
  }

  protected annulerEdition(): void {
    this.idEnEdition.set(null);
    this.modele.set({ libelle: '', contenanceMl: 0 });
  }

  protected async basculerActif(volume: VolumeResponse): Promise<void> {
    await firstValueFrom(
      this.api.volumes.modifier(volume.id, {
        libelle: volume.libelle,
        contenanceMl: volume.contenanceMl,
        actif: !volume.actif,
      }),
    );
    this.notification.succes(volume.actif ? 'Volume désactivé.' : 'Volume activé.');
    this.volumes.reload();
  }
}
