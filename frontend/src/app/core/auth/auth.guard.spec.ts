import { TestBed } from '@angular/core/testing';
import { provideRouter, Router, UrlTree } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { authGuard, invitesGuard, permissionGuard } from './auth.guard';

const CLE_SESSION = 'obv-gestion.session';

/** AuthService lit sa session depuis localStorage à la construction : c'est le seul point d'entrée public pour simuler un état authentifié dans ces tests. */
function seConnecterFictivement(permissions: string[] = []): void {
  localStorage.setItem(
    CLE_SESSION,
    JSON.stringify({ accessToken: 'jeton', refreshToken: 'refresh', utilisateurId: 1, permissions }),
  );
}

function configurerTestBed(): Router {
  TestBed.configureTestingModule({
    providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
  });
  return TestBed.inject(Router);
}

describe('auth.guard', () => {
  beforeEach(() => localStorage.clear());

  it("authGuard refuse et redirige vers /connexion quand l'utilisateur n'est pas authentifié", () => {
    const router = configurerTestBed();
    const resultat = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
    expect(resultat).toBeInstanceOf(UrlTree);
    expect(router.serializeUrl(resultat as UrlTree)).toBe('/connexion');
  });

  it('authGuard autorise (true) quand authentifié', () => {
    seConnecterFictivement();
    configurerTestBed();
    const resultat = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
    expect(resultat).toBe(true);
  });

  it('permissionGuard redirige vers /connexion quand non authentifié', () => {
    const router = configurerTestBed();
    const route = { data: { permission: 'RAPPORT_READ' } } as never;
    const resultat = TestBed.runInInjectionContext(() => permissionGuard(route, {} as never));
    expect(router.serializeUrl(resultat as UrlTree)).toBe('/connexion');
  });

  it("permissionGuard redirige vers /tableau-de-bord quand la permission manque", () => {
    seConnecterFictivement(['VENTE_WRITE']);
    const router = configurerTestBed();
    const route = { data: { permission: 'RAPPORT_READ' } } as never;
    const resultat = TestBed.runInInjectionContext(() => permissionGuard(route, {} as never));
    expect(router.serializeUrl(resultat as UrlTree)).toBe('/tableau-de-bord');
  });

  it('permissionGuard autorise (true) quand la permission requise est détenue', () => {
    seConnecterFictivement(['RAPPORT_READ']);
    configurerTestBed();
    const route = { data: { permission: 'RAPPORT_READ' } } as never;
    const resultat = TestBed.runInInjectionContext(() => permissionGuard(route, {} as never));
    expect(resultat).toBe(true);
  });

  it(
    'invitesGuard renvoie false (jamais une UrlTree) une fois authentifié — régression : un canMatch qui ' +
      "redirige vers sa propre cible boucle indéfiniment et fait planter l'onglet (voir commit GUI G1-G2)",
    () => {
      seConnecterFictivement();
      configurerTestBed();
      const resultat = TestBed.runInInjectionContext(() => invitesGuard({} as never, [] as never, {} as never));
      expect(resultat).toBe(false);
    },
  );

  it("invitesGuard renvoie true quand l'utilisateur n'est pas authentifié", () => {
    configurerTestBed();
    const resultat = TestBed.runInInjectionContext(() => invitesGuard({} as never, [] as never, {} as never));
    expect(resultat).toBe(true);
  });
});
