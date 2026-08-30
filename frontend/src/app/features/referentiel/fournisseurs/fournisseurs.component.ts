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
import { FournisseurResponse } from '../../../core/api/models/referentiel.model';
import { NotificationService } from '../../../core/ui/notification.service';
import { AuthService } from '../../../core/auth/auth.service';

interface FournisseurModele {
  raisonSociale: string;
  telephone: string;
  email: string;
  adresse: string;
}

const vide: FournisseurModele = { raisonSociale: '', telephone: '', email: '', adresse: '' };

const fournisseurSchema = schema<FournisseurModele>((champ) => {
  required(champ.raisonSociale, { message: 'La raison sociale est requise.' });
});

/** §5, §7 — référentiel des fournisseurs (livraisons au dépôt). */
@Component({
  selector: 'app-fournisseurs',
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
  templateUrl: './fournisseurs.component.html',
  styleUrl: '../referentiel-liste.scss',
})
export class FournisseursComponent {
  private readonly api = inject(ReferentielApiService);
  private readonly notification = inject(NotificationService);
  protected readonly auth = inject(AuthService);

  protected readonly colonnes = ['raisonSociale', 'telephone', 'email', 'actif', 'actions'];
  protected readonly enCours = signal(false);
  protected readonly idEnEdition = signal<number | null>(null);

  protected readonly modele = signal<FournisseurModele>({ ...vide });
  protected readonly formulaire = form(this.modele, fournisseurSchema);

  protected readonly fournisseurs = rxResource({
    stream: () => this.api.fournisseurs.lister({}, { size: 100, sort: 'raisonSociale,asc' }),
  });

  protected async onSubmit(event: Event): Promise<void> {
    event.preventDefault();
    await submit(this.formulaire, async (f) => {
      this.enCours.set(true);
      try {
        const valeur = f().value();
        const id = this.idEnEdition();
        if (id === null) {
          await firstValueFrom(this.api.fournisseurs.creer(valeur));
          this.notification.succes('Fournisseur créé.');
        } else {
          const existant = this.fournisseurs.value()?.content.find((x) => x.id === id);
          await firstValueFrom(this.api.fournisseurs.modifier(id, { ...valeur, actif: existant?.actif ?? true }));
          this.notification.succes('Fournisseur modifié.');
        }
        this.annulerEdition();
        this.fournisseurs.reload();
        return undefined;
      } catch {
        return [{ fieldTree: this.formulaire, kind: 'server', message: 'Échec de l’enregistrement.' }];
      } finally {
        this.enCours.set(false);
      }
    });
  }

  protected modifier(fournisseur: FournisseurResponse): void {
    this.idEnEdition.set(fournisseur.id);
    this.modele.set({
      raisonSociale: fournisseur.raisonSociale,
      telephone: fournisseur.telephone ?? '',
      email: fournisseur.email ?? '',
      adresse: fournisseur.adresse ?? '',
    });
  }

  protected annulerEdition(): void {
    this.idEnEdition.set(null);
    this.modele.set({ ...vide });
  }

  protected async basculerActif(fournisseur: FournisseurResponse): Promise<void> {
    await firstValueFrom(
      this.api.fournisseurs.modifier(fournisseur.id, {
        raisonSociale: fournisseur.raisonSociale,
        telephone: fournisseur.telephone,
        email: fournisseur.email,
        adresse: fournisseur.adresse,
        actif: !fournisseur.actif,
      }),
    );
    this.notification.succes(fournisseur.actif ? 'Fournisseur désactivé.' : 'Fournisseur activé.');
    this.fournisseurs.reload();
  }
}
