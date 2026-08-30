import { Component, computed, effect, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { of } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { RapportApiService } from '../../core/api/rapport-api.service';
import { ReferentielApiService } from '../../core/api/referentiel-api.service';
import { LIBELLES_MODE_PAIEMENT } from '../../core/api/models/enums';
import { XofPipe } from '../../shared/pipes/xof.pipe';

interface LienRapide {
  libelle: string;
  route: string;
  icone: string;
  permission?: Parameters<AuthService['aLaPermission']>[0];
}

const LIENS_RAPIDES: LienRapide[] = [
  { libelle: 'Nouvelle vente', route: '/vente', icone: 'point_of_sale', permission: 'VENTE_WRITE' },
  { libelle: 'Tickets serveurs', route: '/tickets-serveur', icone: 'sports_bar', permission: 'VENTE_WRITE' },
  { libelle: 'Nouvelle réception', route: '/receptions', icone: 'local_shipping', permission: 'RECEPTION_WRITE' },
  { libelle: 'Nouveau transfert', route: '/transferts', icone: 'sync_alt', permission: 'TRANSFERT_WRITE' },
  { libelle: 'Rapports', route: '/rapports', icone: 'bar_chart', permission: 'RAPPORT_READ' },
  { libelle: 'Référentiel', route: '/referentiel', icone: 'category', permission: 'REFERENTIEL_WRITE' },
];

function bornesAujourdhui(): { du: string; au: string } {
  const maintenant = new Date();
  const debut = new Date(
    Date.UTC(maintenant.getUTCFullYear(), maintenant.getUTCMonth(), maintenant.getUTCDate(), 0, 0, 0),
  );
  const fin = new Date(
    Date.UTC(maintenant.getUTCFullYear(), maintenant.getUTCMonth(), maintenant.getUTCDate(), 23, 59, 59),
  );
  return { du: debut.toISOString(), au: fin.toISOString() };
}

/** §15.3 — écran d'atterrissage après connexion : KPIs du jour + accès rapides. */
@Component({
  selector: 'app-dashboard',
  imports: [
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatIconModule,
    MatProgressSpinnerModule,
    XofPipe,
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent {
  protected readonly auth = inject(AuthService);
  private readonly referentielApi = inject(ReferentielApiService);
  private readonly rapportApi = inject(RapportApiService);

  protected readonly libellesModePaiement: Record<string, string> = LIBELLES_MODE_PAIEMENT;
  protected readonly lienRapides = LIENS_RAPIDES.filter(
    (lien) => !lien.permission || this.auth.aLaPermission(lien.permission),
  );

  protected readonly pdvSelectionne = signal<number | null>(null);

  protected readonly pointsDeVente = rxResource({
    stream: () => this.referentielApi.pointsDeVente.lister({ actif: true }, { size: 100 }),
  });

  protected readonly rapportDuJour = rxResource({
    params: () => this.pdvSelectionne(),
    stream: ({ params }) => {
      if (params === null || !this.auth.aLaPermission('RAPPORT_READ')) {
        return of(undefined);
      }
      return this.rapportApi.ventes({ pdv: params, ...bornesAujourdhui() });
    },
  });

  protected readonly entreesModePaiement = computed(() => {
    const rapport = this.rapportDuJour.value();
    return rapport ? Object.entries(rapport.recetteParModePaiementXof) : [];
  });

  protected readonly entreesParServeur = computed(() => {
    const rapport = this.rapportDuJour.value();
    return rapport ? Object.entries(rapport.quantiteParServeur) : [];
  });

  constructor() {
    effect(() => {
      const pdvs = this.pointsDeVente.value();
      if (pdvs && pdvs.content.length > 0 && this.pdvSelectionne() === null) {
        this.pdvSelectionne.set(pdvs.content[0].id);
      }
    });
  }
}
