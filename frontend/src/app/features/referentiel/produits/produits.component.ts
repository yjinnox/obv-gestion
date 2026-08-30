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
import { form, FormField, min, required, schema, submit } from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { ReferentielApiService } from '../../../core/api/referentiel-api.service';
import { ProduitResponse } from '../../../core/api/models/referentiel.model';
import { NotificationService } from '../../../core/ui/notification.service';
import { AuthService } from '../../../core/auth/auth.service';
import { XofPipe } from '../../../shared/pipes/xof.pipe';

interface ProduitModele {
  marqueId: number | null;
  volumeId: number | null;
  montantConsigneXof: number;
}

const videCreation: ProduitModele = { marqueId: null, volumeId: null, montantConsigneXof: 0 };

const produitSchema = schema<ProduitModele>((champ) => {
  required(champ.marqueId, { message: 'La marque est requise.' });
  required(champ.volumeId, { message: 'Le volume est requis.' });
  min(champ.montantConsigneXof, 0, { message: 'La consigne ne peut pas être négative.' });
});

/** §5 — référentiel des produits (couple marque + volume). */
@Component({
  selector: 'app-produits',
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
    XofPipe,
  ],
  templateUrl: './produits.component.html',
  styleUrl: '../referentiel-liste.scss',
})
export class ProduitsComponent {
  private readonly api = inject(ReferentielApiService);
  private readonly notification = inject(NotificationService);
  protected readonly auth = inject(AuthService);

  protected readonly colonnes = ['produit', 'montantConsigneXof', 'actif', 'actions'];
  protected readonly enCours = signal(false);
  protected readonly idEnEdition = signal<number | null>(null);

  protected readonly modele = signal<ProduitModele>({ ...videCreation });
  protected readonly formulaire = form(this.modele, produitSchema);

  protected readonly marques = rxResource({ stream: () => this.api.marques.lister({ actif: true }, { size: 200 }) });
  protected readonly volumes = rxResource({ stream: () => this.api.volumes.lister({ actif: true }, { size: 200 }) });

  protected readonly produits = rxResource({
    stream: () => this.api.produits.lister({}, { size: 100 }),
  });

  protected async onSubmit(event: Event): Promise<void> {
    event.preventDefault();
    await submit(this.formulaire, async (f) => {
      this.enCours.set(true);
      try {
        const valeur = f().value();
        const id = this.idEnEdition();
        if (id === null) {
          if (valeur.marqueId === null || valeur.volumeId === null) {
            return undefined;
          }
          await firstValueFrom(
            this.api.produits.creer({ marqueId: valeur.marqueId, volumeId: valeur.volumeId }),
          );
          this.notification.succes('Produit créé.');
        } else {
          const existant = this.produits.value()?.content.find((x) => x.id === id);
          await firstValueFrom(
            this.api.produits.modifier(id, {
              montantConsigneXof: valeur.montantConsigneXof,
              actif: existant?.actif ?? true,
            }),
          );
          this.notification.succes('Produit modifié.');
        }
        this.annulerEdition();
        this.produits.reload();
        return undefined;
      } catch {
        return [{ fieldTree: this.formulaire, kind: 'server', message: 'Échec de l’enregistrement.' }];
      } finally {
        this.enCours.set(false);
      }
    });
  }

  protected modifier(produit: ProduitResponse): void {
    this.idEnEdition.set(produit.id);
    this.modele.set({
      marqueId: produit.marqueId,
      volumeId: produit.volumeId,
      montantConsigneXof: produit.montantConsigneXof,
    });
  }

  protected annulerEdition(): void {
    this.idEnEdition.set(null);
    this.modele.set({ ...videCreation });
  }

  protected async basculerActif(produit: ProduitResponse): Promise<void> {
    await firstValueFrom(
      this.api.produits.modifier(produit.id, {
        montantConsigneXof: produit.montantConsigneXof,
        actif: !produit.actif,
      }),
    );
    this.notification.succes(produit.actif ? 'Produit désactivé.' : 'Produit activé.');
    this.produits.reload();
  }
}
