import { HttpContext, HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { MatBottomSheet } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { form, FormField, required, schema, submit } from '@angular/forms/signals';
import { catchError, firstValueFrom, of, throwError } from 'rxjs';
import { PanierApiService } from '../../core/api/panier-api.service';
import { ReferentielApiService } from '../../core/api/referentiel-api.service';
import { SessionVenteApiService } from '../../core/api/session-vente-api.service';
import { ProduitResponse } from '../../core/api/models/referentiel.model';
import { AuthService } from '../../core/auth/auth.service';
import { NotificationService } from '../../core/ui/notification.service';
import { SANS_TOAST_ERREUR } from '../../core/http/http-context';
import { XofPipe } from '../../shared/pipes/xof.pipe';
import { CheckoutSheetComponent } from './checkout-sheet.component';

interface OuvertureModele {
  pointDeVenteId: number | null;
  fondCaisseXof: number;
}

const ouvertureSchema = schema<OuvertureModele>((champ) => {
  required(champ.pointDeVenteId, { message: 'Le point de vente est requis.' });
  required(champ.fondCaisseXof, { message: 'Le fond de caisse est requis.' });
});

/** §8.2, §15.3 — vente dépôt : sélection produit en 3 taps max, total permanent en pied d'écran. */
@Component({
  selector: 'app-vente',
  imports: [
    RouterLink,
    FormField,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    XofPipe,
  ],
  templateUrl: './vente.component.html',
  styleUrl: './vente.component.scss',
})
export class VenteComponent {
  private readonly panierApi = inject(PanierApiService);
  private readonly referentielApi = inject(ReferentielApiService);
  private readonly sessionApi = inject(SessionVenteApiService);
  private readonly notification = inject(NotificationService);
  private readonly bottomSheet = inject(MatBottomSheet);
  protected readonly auth = inject(AuthService);

  protected readonly enCours = signal(false);
  protected readonly pdvSelectionne = signal<number | null>(null);

  // Un tap rapide sur le stepper (§15.3) peut déclencher un second clic
  // avant que le premier aller-retour réseau n'ait rechargé le panier :
  // sans ce verrou, `incrementer`/`decrementer` liraient tous deux
  // l'ancienne quantité et produiraient deux ajouts au lieu d'un
  // incrément de deux.
  protected readonly produitsEnCours = signal<ReadonlySet<number>>(new Set());

  protected readonly pointsDeVente = rxResource({
    stream: () => this.referentielApi.pointsDeVente.lister({ actif: true }, { size: 100 }),
  });
  protected readonly pointsDeVenteDepot = computed(
    () => this.pointsDeVente.value()?.content.filter((p) => p.type === 'DEPOT') ?? [],
  );

  protected readonly modeleOuverture = signal<OuvertureModele>({ pointDeVenteId: null, fondCaisseXof: 0 });
  protected readonly formulaireOuverture = form(this.modeleOuverture, ouvertureSchema);

  // §8.1 — /sessions-vente/courante répond 404 quand aucune session n'est
  // ouverte (état normal ici, pas une erreur) : rxResource re-lève toute
  // erreur HTTP à la lecture de `.value()`, donc on l'intercepte pour
  // obtenir un `undefined` propre plutôt qu'un état d'erreur du resource.
  protected readonly sessionCourante = rxResource({
    params: () => this.pdvSelectionne(),
    stream: ({ params }) =>
      params === null
        ? of(undefined)
        : this.sessionApi.courante(params, new HttpContext().set(SANS_TOAST_ERREUR, true)).pipe(
            catchError((erreur: unknown) =>
              erreur instanceof HttpErrorResponse && erreur.status === 404 ? of(undefined) : throwError(() => erreur),
            ),
          ),
  });

  protected readonly produits = rxResource({
    stream: () => this.referentielApi.produits.lister({ actif: true }, { size: 200 }),
  });

  protected readonly tarifs = rxResource({
    params: () => this.pdvSelectionne(),
    stream: ({ params }) =>
      params === null
        ? of(undefined)
        : this.referentielApi.listerTarifs({ pdv: params, nature: 'VENTE' }, { size: 200 }),
  });

  protected readonly panier = rxResource({
    params: () => this.sessionCourante.value()?.id ?? null,
    stream: ({ params }) => (params === null ? of(undefined) : this.panierApi.obtenir(params)),
  });

  protected readonly produitsVendables = computed(() => {
    // dateFin absent (pas seulement `null`) : le JSON omet le champ quand
    // il est vide plutôt que d'envoyer `"dateFin":null` — `!t.dateFin`
    // couvre donc `undefined` aussi bien que `null`.
    const tarifsActifs = (this.tarifs.value()?.content ?? []).filter((t) => !t.dateFin && t.uniteVente === 'CASIER');
    const parProduit = new Map(tarifsActifs.map((t) => [t.produitId, t.montantXof]));
    return (this.produits.value()?.content ?? [])
      .filter((p) => parProduit.has(p.id))
      .map((p) => ({ produit: p, prixXof: parProduit.get(p.id)! }));
  });

  protected readonly totalPanierXof = computed(() => this.panier.value()?.montantGlobalXof ?? 0);
  protected readonly nombreArticles = computed(
    () => this.panier.value()?.lignes.reduce((s, l) => s + l.quantiteDemiCasiers, 0) ?? 0,
  );

  quantiteDansPanier(produit: ProduitResponse): number {
    return this.panier.value()?.lignes.find((l) => l.produitId === produit.id)?.quantiteDemiCasiers ?? 0;
  }

  private ligneDuPanier(produit: ProduitResponse) {
    return this.panier.value()?.lignes.find((l) => l.produitId === produit.id);
  }

  protected estEnCours(produit: ProduitResponse): boolean {
    return this.produitsEnCours().has(produit.id);
  }

  private marquerEnCours(produitId: number, enCours: boolean): void {
    this.produitsEnCours.update((ids) => {
      const copie = new Set(ids);
      if (enCours) {
        copie.add(produitId);
      } else {
        copie.delete(produitId);
      }
      return copie;
    });
  }

  protected async incrementer(produit: ProduitResponse): Promise<void> {
    const session = this.sessionCourante.value();
    if (!session || this.estEnCours(produit)) return;
    this.marquerEnCours(produit.id, true);
    try {
      const ligne = this.ligneDuPanier(produit);
      if (ligne) {
        await firstValueFrom(
          this.panierApi.modifierLigne(ligne.id, {
            sessionVenteId: session.id,
            quantiteDemiCasiers: ligne.quantiteDemiCasiers + 1,
          }),
        );
      } else {
        await firstValueFrom(
          this.panierApi.ajouterLigne({ sessionVenteId: session.id, produitId: produit.id, quantiteDemiCasiers: 1 }),
        );
      }
      this.panier.reload();
    } finally {
      this.marquerEnCours(produit.id, false);
    }
  }

  protected async decrementer(produit: ProduitResponse): Promise<void> {
    const session = this.sessionCourante.value();
    const ligne = this.ligneDuPanier(produit);
    if (!session || !ligne || this.estEnCours(produit)) return;
    this.marquerEnCours(produit.id, true);
    try {
      if (ligne.quantiteDemiCasiers <= 1) {
        await firstValueFrom(this.panierApi.supprimerLigne(ligne.id, session.id));
      } else {
        await firstValueFrom(
          this.panierApi.modifierLigne(ligne.id, {
            sessionVenteId: session.id,
            quantiteDemiCasiers: ligne.quantiteDemiCasiers - 1,
          }),
        );
      }
      this.panier.reload();
    } finally {
      this.marquerEnCours(produit.id, false);
    }
  }

  protected async onSubmitOuverture(event: Event): Promise<void> {
    event.preventDefault();
    await submit(this.formulaireOuverture, async (f) => {
      this.enCours.set(true);
      try {
        const valeur = f().value();
        if (valeur.pointDeVenteId === null) return undefined;
        await firstValueFrom(
          this.sessionApi.ouvrir({ pointDeVenteId: valeur.pointDeVenteId, fondCaisseXof: valeur.fondCaisseXof }),
        );
        this.notification.succes('Session ouverte.');
        this.sessionCourante.reload();
        return undefined;
      } catch {
        return [{ fieldTree: this.formulaireOuverture, kind: 'server', message: "Échec de l'ouverture." }];
      } finally {
        this.enCours.set(false);
      }
    });
  }

  protected ouvrirPanier(): void {
    const session = this.sessionCourante.value();
    const panier = this.panier.value();
    if (!session || !panier) return;
    const ref = this.bottomSheet.open(CheckoutSheetComponent, { data: { session, panier } });
    ref.afterDismissed().subscribe((commandePassee) => {
      if (commandePassee) {
        this.panier.reload();
      }
    });
  }
}
