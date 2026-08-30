import { Component, computed, inject, signal } from '@angular/core';
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
import { ServeurResponse } from '../../../core/api/models/referentiel.model';
import { NotificationService } from '../../../core/ui/notification.service';
import { AuthService } from '../../../core/auth/auth.service';

interface ServeurModele {
  pointDeVenteId: number | null;
  nom: string;
  prenoms: string;
  telephone: string;
}

const videCreation: ServeurModele = { pointDeVenteId: null, nom: '', prenoms: '', telephone: '' };

const serveurSchema = schema<ServeurModele>((champ) => {
  required(champ.pointDeVenteId, { message: 'Le point de vente est requis.' });
  required(champ.nom, { message: 'Le nom est requis.' });
  required(champ.prenoms, { message: 'Les prénoms sont requis.' });
});

/** §5, RG-33 — référentiel des serveurs d'un bar/maquis. */
@Component({
  selector: 'app-serveurs',
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
  templateUrl: './serveurs.component.html',
  styleUrl: '../referentiel-liste.scss',
})
export class ServeursComponent {
  private readonly api = inject(ReferentielApiService);
  private readonly notification = inject(NotificationService);
  protected readonly auth = inject(AuthService);

  protected readonly colonnes = ['nom', 'pointDeVente', 'telephone', 'actif', 'actions'];
  protected readonly enCours = signal(false);
  protected readonly idEnEdition = signal<number | null>(null);

  protected readonly modele = signal<ServeurModele>({ ...videCreation });
  protected readonly formulaire = form(this.modele, serveurSchema);

  protected readonly pointsDeVenteBar = rxResource({
    stream: () => this.api.pointsDeVente.lister({ actif: true }, { size: 100 }),
  });
  protected readonly optionsPointDeVenteBar = computed(
    () => this.pointsDeVenteBar.value()?.content.filter((p) => p.type === 'BAR') ?? [],
  );

  protected readonly serveurs = rxResource({
    stream: () => this.api.serveurs.lister({}, { size: 100 }),
  });

  libellePointDeVente(id: number): string {
    return this.pointsDeVenteBar.value()?.content.find((p) => p.id === id)?.libelle ?? `#${id}`;
  }

  protected async onSubmit(event: Event): Promise<void> {
    event.preventDefault();
    await submit(this.formulaire, async (f) => {
      this.enCours.set(true);
      try {
        const valeur = f().value();
        if (valeur.pointDeVenteId === null) {
          return undefined;
        }
        const requete = { ...valeur, pointDeVenteId: valeur.pointDeVenteId };
        const id = this.idEnEdition();
        if (id === null) {
          await firstValueFrom(this.api.serveurs.creer(requete));
          this.notification.succes('Serveur créé.');
        } else {
          const existant = this.serveurs.value()?.content.find((x) => x.id === id);
          await firstValueFrom(
            this.api.serveurs.modifier(id, {
              nom: valeur.nom,
              prenoms: valeur.prenoms,
              telephone: valeur.telephone,
              actif: existant?.actif ?? true,
            }),
          );
          this.notification.succes('Serveur modifié.');
        }
        this.annulerEdition();
        this.serveurs.reload();
        return undefined;
      } catch {
        return [{ fieldTree: this.formulaire, kind: 'server', message: 'Échec de l’enregistrement.' }];
      } finally {
        this.enCours.set(false);
      }
    });
  }

  protected modifier(serveur: ServeurResponse): void {
    this.idEnEdition.set(serveur.id);
    this.modele.set({
      pointDeVenteId: serveur.pointDeVenteId,
      nom: serveur.nom,
      prenoms: serveur.prenoms,
      telephone: serveur.telephone ?? '',
    });
  }

  protected annulerEdition(): void {
    this.idEnEdition.set(null);
    this.modele.set({ ...videCreation });
  }

  protected async basculerActif(serveur: ServeurResponse): Promise<void> {
    await firstValueFrom(
      this.api.serveurs.modifier(serveur.id, {
        nom: serveur.nom,
        prenoms: serveur.prenoms,
        telephone: serveur.telephone,
        actif: !serveur.actif,
      }),
    );
    this.notification.succes(serveur.actif ? 'Serveur désactivé.' : 'Serveur activé.');
    this.serveurs.reload();
  }
}
