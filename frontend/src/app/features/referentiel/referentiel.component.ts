import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatTabsModule } from '@angular/material/tabs';

const ONGLETS = [
  { libelle: 'Marques', route: 'marques' },
  { libelle: 'Volumes', route: 'volumes' },
  { libelle: 'Produits', route: 'produits' },
  { libelle: 'Conditionnements', route: 'conditionnements' },
  { libelle: 'Tarifs', route: 'tarifs' },
  { libelle: 'Points de vente', route: 'points-de-vente' },
  { libelle: 'Fournisseurs', route: 'fournisseurs' },
  { libelle: 'Clients', route: 'clients' },
  { libelle: 'Serveurs', route: 'serveurs' },
];

/** §5 — hub du référentiel : un onglet par entité, chacune sa propre route. */
@Component({
  selector: 'app-referentiel',
  imports: [RouterLink, RouterLinkActive, RouterOutlet, MatTabsModule],
  template: `
    <nav mat-tab-nav-bar [tabPanel]="panneau" class="referentiel-nav">
      @for (onglet of onglets; track onglet.route) {
        <a mat-tab-link [routerLink]="onglet.route" routerLinkActive #rla="routerLinkActive" [active]="rla.isActive">
          {{ onglet.libelle }}
        </a>
      }
    </nav>
    <mat-tab-nav-panel #panneau>
      <router-outlet />
    </mat-tab-nav-panel>
  `,
  styles: `
    .referentiel-nav {
      margin-bottom: 16px;
      overflow-x: auto;
    }
  `,
})
export class ReferentielComponent {
  protected readonly onglets = ONGLETS;
}
