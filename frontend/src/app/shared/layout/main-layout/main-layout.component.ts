import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { map } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { NAV_ITEMS } from '../nav-items';

/**
 * Coquille des écrans authentifiés : barre d'outils + navigation latérale
 * responsive (§15.3, mobile-first — repliée en superposition sur téléphone,
 * fixe sur écran large).
 */
@Component({
  selector: 'app-main-layout',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatMenuModule,
    MatButtonModule,
  ],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.scss',
})
export class MainLayoutComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly breakpointObserver = inject(BreakpointObserver);

  private readonly estMobile = toSignal(
    this.breakpointObserver.observe(Breakpoints.Handset).pipe(map((r) => r.matches)),
    { initialValue: false },
  );

  readonly modeSidenav = computed(() => (this.estMobile() ? 'over' : 'side'));
  readonly sidenavOuverte = signal(true);

  readonly navItems = computed(() => NAV_ITEMS.filter((item) => !item.permission || this.auth.aLaPermission(item.permission)));

  constructor() {
    this.sidenavOuverte.set(!this.estMobile());
  }

  basculerSidenav(): void {
    this.sidenavOuverte.update((v) => !v);
  }

  fermerSiMobile(): void {
    if (this.estMobile()) {
      this.sidenavOuverte.set(false);
    }
  }

  seDeconnecter(): void {
    this.auth.deconnecter();
    this.router.navigateByUrl('/connexion');
  }
}
