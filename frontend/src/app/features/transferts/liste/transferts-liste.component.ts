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
import { firstValueFrom } from 'rxjs';
import { TransfertApiService } from '../../../core/api/transfert-api.service';
import { ReferentielApiService } from '../../../core/api/referentiel-api.service';
import { LigneTransfertRequest } from '../../../core/api/models/transfert.model';
import { LIBELLES_STATUT_TRANSFERT, StatutTransfert } from '../../../core/api/models/enums';
import { AuthService } from '../../../core/auth/auth.service';
import { XofPipe } from '../../../shared/pipes/xof.pipe';

function maintenantLocal(): string {
  const d = new Date();
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 16);
}

/**
 * §9 — liste des transferts + création. L'API n'a pas d'endpoint de
 * lignes séparé (contrairement aux réceptions) : le bon complet (en-tête
 * + lignes) est créé en un seul appel, d'où le constructeur de lignes
 * local avant soumission.
 */
@Component({
  selector: 'app-transferts-liste',
  imports: [
    DatePipe,
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
  templateUrl: './transferts-liste.component.html',
  styleUrl: './transferts-liste.component.scss',
})
export class TransfertsListeComponent {
  private readonly api = inject(TransfertApiService);
  private readonly referentielApi = inject(ReferentielApiService);
  private readonly router = inject(Router);
  protected readonly auth = inject(AuthService);

  protected readonly libellesStatut: Record<string, string> = LIBELLES_STATUT_TRANSFERT;
  protected readonly colonnes = ['numero', 'source', 'destination', 'dateHeure', 'statut', 'montantTotalXof'];

  protected readonly afficherCreation = signal(false);
  protected readonly enCours = signal(false);
  protected readonly filtreStatut = signal<StatutTransfert | null>(null);

  protected readonly pointDeVenteSourceId = signal<number | null>(null);
  protected readonly pointDeVenteDestinationId = signal<number | null>(null);
  protected readonly dateHeure = signal<string>(maintenantLocal());
  protected readonly lignes = signal<LigneTransfertRequest[]>([]);

  protected readonly produitAAjouter = signal<number | null>(null);
  protected readonly conditionnementAAjouter = signal<number | null>(null);
  protected readonly quantiteAAjouter = signal<number>(1);

  protected readonly pointsDeVente = rxResource({
    stream: () => this.referentielApi.pointsDeVente.lister({ actif: true }, { size: 100 }),
  });
  protected readonly pointsDeVenteSource = computed(
    () => this.pointsDeVente.value()?.content.filter((p) => p.type === 'DEPOT') ?? [],
  );
  protected readonly pointsDeVenteDestination = computed(
    () => this.pointsDeVente.value()?.content.filter((p) => p.type === 'BAR') ?? [],
  );

  protected readonly produits = rxResource({
    stream: () => this.referentielApi.produits.lister({ actif: true }, { size: 200 }),
  });
  protected readonly conditionnements = rxResource({
    stream: () => this.referentielApi.conditionnements.lister({ actif: true }, { size: 200 }),
  });
  // RG-11 — le transfert se fait exclusivement en demi-casiers : un
  // conditionnement à capacité impaire (demiCasierAutorise=false) est
  // exclu ici plutôt que de laisser l'utilisateur le sélectionner puis
  // essuyer un 409 à la création.
  protected readonly conditionnementsDuProduit = computed(
    () =>
      this.conditionnements
        .value()
        ?.content.filter((c) => c.produitId === this.produitAAjouter() && c.demiCasierAutorise) ?? [],
  );

  protected readonly transferts = rxResource({
    params: () => this.filtreStatut(),
    stream: ({ params }) =>
      this.api.lister({ statut: params ?? undefined }, { size: 50, sort: 'dateHeure,desc' }),
  });

  libelleProduit(produitId: number): string {
    const p = this.produits.value()?.content.find((x) => x.id === produitId);
    return p ? `${p.marqueLibelle} ${p.volumeLibelle}` : `#${produitId}`;
  }

  protected ajouterLigne(): void {
    if (this.produitAAjouter() === null || this.conditionnementAAjouter() === null || this.quantiteAAjouter() <= 0) {
      return;
    }
    this.lignes.update((liste) => [
      ...liste,
      {
        produitId: this.produitAAjouter()!,
        conditionnementId: this.conditionnementAAjouter()!,
        quantiteDemiCasiers: this.quantiteAAjouter(),
      },
    ]);
    this.produitAAjouter.set(null);
    this.conditionnementAAjouter.set(null);
    this.quantiteAAjouter.set(1);
  }

  protected retirerLigne(index: number): void {
    this.lignes.update((liste) => liste.filter((_, i) => i !== index));
  }

  protected async creer(): Promise<void> {
    if (this.pointDeVenteSourceId() === null || this.pointDeVenteDestinationId() === null) {
      return;
    }
    if (this.lignes().length === 0) {
      return;
    }
    this.enCours.set(true);
    try {
      const transfert = await firstValueFrom(
        this.api.creer({
          pointDeVenteSourceId: this.pointDeVenteSourceId()!,
          pointDeVenteDestinationId: this.pointDeVenteDestinationId()!,
          dateHeure: new Date(this.dateHeure()).toISOString(),
          lignes: this.lignes(),
        }),
      );
      await this.router.navigate(['/transferts', transfert.id]);
    } finally {
      this.enCours.set(false);
    }
  }

  protected ouvrir(id: number): void {
    this.router.navigate(['/transferts', id]);
  }
}
