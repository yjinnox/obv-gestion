import { Component, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { form, FormField, required, schema, submit } from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { ReferentielApiService } from '../../../core/api/referentiel-api.service';
import { NotificationService } from '../../../core/ui/notification.service';
import { AuthService } from '../../../core/auth/auth.service';
import { XofPipe } from '../../../shared/pipes/xof.pipe';

interface TarifModele {
  pointDeVenteId: number | null;
  produitId: number | null;
  uniteVente: 'CASIER' | 'BOUTEILLE';
  nature: 'ACHAT' | 'VENTE' | 'CESSION';
  montantXof: number;
  dateDebut: string;
}

function aujourdhuiIso(): string {
  return new Date().toISOString().slice(0, 10);
}

const videCreation: TarifModele = {
  pointDeVenteId: null,
  produitId: null,
  uniteVente: 'CASIER',
  nature: 'VENTE',
  montantXof: 0,
  dateDebut: aujourdhuiIso(),
};

const tarifSchema = schema<TarifModele>((champ) => {
  required(champ.pointDeVenteId, { message: 'Le point de vente est requis.' });
  required(champ.produitId, { message: 'Le produit est requis.' });
  required(champ.montantXof, { message: 'Le montant est requis.' });
  required(champ.dateDebut, { message: 'La date de début est requise.' });
});

/**
 * §5.2, RG-08/RG-09 — tarification datée : jamais de modification/suppression,
 * une nouvelle création clôt automatiquement le tarif ouvert pour la même clé.
 */
@Component({
  selector: 'app-tarifs',
  imports: [
    FormField,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatTableModule,
    XofPipe,
  ],
  templateUrl: './tarifs.component.html',
  styleUrl: '../referentiel-liste.scss',
})
export class TarifsComponent {
  private readonly api = inject(ReferentielApiService);
  private readonly notification = inject(NotificationService);
  protected readonly auth = inject(AuthService);

  protected readonly colonnes = ['pointDeVente', 'produit', 'nature', 'uniteVente', 'montantXof', 'periode'];
  protected readonly enCours = signal(false);

  protected readonly modele = signal<TarifModele>({ ...videCreation });
  protected readonly formulaire = form(this.modele, tarifSchema);

  protected readonly pointsDeVente = rxResource({
    stream: () => this.api.pointsDeVente.lister({ actif: true }, { size: 100 }),
  });
  protected readonly produits = rxResource({
    stream: () => this.api.produits.lister({ actif: true }, { size: 200 }),
  });

  protected readonly filtrePdv = signal<number | null>(null);
  protected readonly tarifs = rxResource({
    params: () => this.filtrePdv(),
    stream: ({ params }) =>
      this.api.listerTarifs(params === null ? {} : { pdv: params }, { size: 100, sort: 'dateDebut,desc' }),
  });

  libellePointDeVente(id: number): string {
    return this.pointsDeVente.value()?.content.find((p) => p.id === id)?.libelle ?? `#${id}`;
  }

  libelleProduit(id: number): string {
    const produit = this.produits.value()?.content.find((p) => p.id === id);
    return produit ? `${produit.marqueLibelle} ${produit.volumeLibelle}` : `#${id}`;
  }

  protected async onSubmit(event: Event): Promise<void> {
    event.preventDefault();
    await submit(this.formulaire, async (f) => {
      this.enCours.set(true);
      try {
        const valeur = f().value();
        if (valeur.pointDeVenteId === null || valeur.produitId === null) {
          return undefined;
        }
        await firstValueFrom(
          this.api.creerTarif({
            pointDeVenteId: valeur.pointDeVenteId,
            produitId: valeur.produitId,
            uniteVente: valeur.uniteVente,
            nature: valeur.nature,
            montantXof: valeur.montantXof,
            dateDebut: valeur.dateDebut,
          }),
        );
        this.notification.succes('Tarif créé (le tarif précédent, s’il existait, a été clôturé).');
        this.modele.set({ ...videCreation });
        this.tarifs.reload();
        return undefined;
      } catch {
        return [{ fieldTree: this.formulaire, kind: 'server', message: 'Échec de l’enregistrement.' }];
      } finally {
        this.enCours.set(false);
      }
    });
  }
}
