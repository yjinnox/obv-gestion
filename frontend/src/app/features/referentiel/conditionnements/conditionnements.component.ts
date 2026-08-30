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
import { ConditionnementResponse } from '../../../core/api/models/referentiel.model';
import { NotificationService } from '../../../core/ui/notification.service';
import { AuthService } from '../../../core/auth/auth.service';

interface ConditionnementModele {
  produitId: number | null;
  capaciteBouteilles: number;
}

const videCreation: ConditionnementModele = { produitId: null, capaciteBouteilles: 12 };

const conditionnementSchema = schema<ConditionnementModele>((champ) => {
  required(champ.produitId, { message: 'Le produit est requis.' });
  required(champ.capaciteBouteilles, { message: 'La capacité est requise.' });
  min(champ.capaciteBouteilles, 1, { message: 'La capacité doit être positive.' });
});

/** §5, RG-11 — référentiel des conditionnements (casiers) d'un produit. */
@Component({
  selector: 'app-conditionnements',
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
  templateUrl: './conditionnements.component.html',
  styleUrl: '../referentiel-liste.scss',
})
export class ConditionnementsComponent {
  private readonly api = inject(ReferentielApiService);
  private readonly notification = inject(NotificationService);
  protected readonly auth = inject(AuthService);

  protected readonly colonnes = ['produit', 'capaciteBouteilles', 'demiCasierAutorise', 'actif', 'actions'];
  protected readonly enCours = signal(false);
  protected readonly idEnEdition = signal<number | null>(null);

  protected readonly modele = signal<ConditionnementModele>({ ...videCreation });
  protected readonly formulaire = form(this.modele, conditionnementSchema);

  protected readonly produits = rxResource({
    stream: () => this.api.produits.lister({ actif: true }, { size: 200 }),
  });

  protected readonly conditionnements = rxResource({
    stream: () => this.api.conditionnements.lister({}, { size: 100 }),
  });

  libelleProduit(produitId: number): string {
    const produit = this.produits.value()?.content.find((p) => p.id === produitId);
    return produit ? `${produit.marqueLibelle} ${produit.volumeLibelle}` : `#${produitId}`;
  }

  protected async onSubmit(event: Event): Promise<void> {
    event.preventDefault();
    await submit(this.formulaire, async (f) => {
      this.enCours.set(true);
      try {
        const valeur = f().value();
        if (valeur.produitId === null) {
          return undefined;
        }
        const id = this.idEnEdition();
        if (id === null) {
          await firstValueFrom(
            this.api.conditionnements.creer({
              produitId: valeur.produitId,
              capaciteBouteilles: valeur.capaciteBouteilles,
            }),
          );
          this.notification.succes('Conditionnement créé.');
        } else {
          const existant = this.conditionnements.value()?.content.find((x) => x.id === id);
          await firstValueFrom(this.api.conditionnements.modifier(id, { actif: existant ? !existant.actif : true }));
          this.notification.succes('Conditionnement modifié.');
        }
        this.annulerEdition();
        this.conditionnements.reload();
        return undefined;
      } catch {
        return [{ fieldTree: this.formulaire, kind: 'server', message: 'Échec de l’enregistrement.' }];
      } finally {
        this.enCours.set(false);
      }
    });
  }

  protected annulerEdition(): void {
    this.idEnEdition.set(null);
    this.modele.set({ ...videCreation });
  }

  protected async basculerActif(conditionnement: ConditionnementResponse): Promise<void> {
    await firstValueFrom(this.api.conditionnements.modifier(conditionnement.id, { actif: !conditionnement.actif }));
    this.notification.succes(conditionnement.actif ? 'Conditionnement désactivé.' : 'Conditionnement activé.');
    this.conditionnements.reload();
  }
}
