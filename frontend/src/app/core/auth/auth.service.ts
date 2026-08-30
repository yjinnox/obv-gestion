import { HttpClient, HttpContext } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ConnexionResponse, Permission } from './auth.model';
import { TokenStorageService } from './token-storage.service';
import { SANS_TOAST_ERREUR } from '../http/http-context';

const SANS_TOAST = new HttpContext().set(SANS_TOAST_ERREUR, true);

/**
 * État d'authentification courant (§4.4). Les jetons sont conservés en
 * mémoire (signals) et persistés en `localStorage` pour survivre à un
 * rechargement de page ; le rafraîchissement automatique est géré par
 * {@link authInterceptor}, pas ce service (qui expose seulement les
 * opérations et l'état).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly stockage = inject(TokenStorageService);

  private readonly accessTokenSig = signal<string | null>(null);
  private readonly refreshTokenSig = signal<string | null>(null);
  private readonly utilisateurIdSig = signal<number | null>(null);
  private readonly permissionsSig = signal<ReadonlySet<Permission>>(new Set());

  readonly utilisateurId = this.utilisateurIdSig.asReadonly();
  readonly permissions = this.permissionsSig.asReadonly();
  readonly estAuthentifie = computed(() => this.accessTokenSig() !== null);

  constructor() {
    const session = this.stockage.charger();
    if (session) {
      this.accessTokenSig.set(session.accessToken);
      this.refreshTokenSig.set(session.refreshToken);
      this.utilisateurIdSig.set(session.utilisateurId);
      this.permissionsSig.set(new Set(session.permissions));
    }
  }

  accessToken(): string | null {
    return this.accessTokenSig();
  }

  refreshToken(): string | null {
    return this.refreshTokenSig();
  }

  aLaPermission(permission: Permission): boolean {
    return this.permissionsSig().has(permission);
  }

  connecter(identifiant: string, motDePasse: string): Observable<ConnexionResponse> {
    return this.http
      .post<ConnexionResponse>('/api/v1/auth/login', { identifiant, motDePasse }, { context: SANS_TOAST })
      .pipe(tap((reponse) => this.appliquerSession(reponse)));
  }

  /** Utilisé par l'intercepteur HTTP pour le rafraîchissement automatique. */
  rafraichir(): Observable<ConnexionResponse> {
    return this.http
      .post<ConnexionResponse>(
        '/api/v1/auth/refresh',
        { refreshToken: this.refreshTokenSig() },
        { context: SANS_TOAST },
      )
      .pipe(tap((reponse) => this.appliquerSession(reponse)));
  }

  deconnecter(): void {
    const refreshToken = this.refreshTokenSig();
    this.effacerSession();
    if (refreshToken) {
      // Best-effort : la session locale est déjà effacée, peu importe le résultat.
      this.http
        .post('/api/v1/auth/logout', { refreshToken }, { context: SANS_TOAST })
        .subscribe({ error: () => undefined });
    }
  }

  private appliquerSession(reponse: ConnexionResponse): void {
    const permissions = reponse.permissions as Permission[];
    this.accessTokenSig.set(reponse.accessToken);
    this.refreshTokenSig.set(reponse.refreshToken);
    this.utilisateurIdSig.set(reponse.utilisateurId);
    this.permissionsSig.set(new Set(permissions));
    this.stockage.enregistrer({
      accessToken: reponse.accessToken,
      refreshToken: reponse.refreshToken,
      utilisateurId: reponse.utilisateurId,
      permissions,
    });
  }

  private effacerSession(): void {
    this.accessTokenSig.set(null);
    this.refreshTokenSig.set(null);
    this.utilisateurIdSig.set(null);
    this.permissionsSig.set(new Set());
    this.stockage.effacer();
  }
}
