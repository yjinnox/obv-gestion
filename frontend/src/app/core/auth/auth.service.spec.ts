import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';
import { ConnexionResponse } from './auth.model';
import { TokenStorageService } from './token-storage.service';

const REPONSE_CONNEXION: ConnexionResponse = {
  accessToken: 'jeton-acces',
  refreshToken: 'jeton-refresh',
  utilisateurId: 42,
  permissions: ['VENTE_WRITE', 'RECEPTION_READ'],
};

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let stockage: TokenStorageService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    stockage = TestBed.inject(TokenStorageService);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it("n'est pas authentifié tant qu'aucune session n'a été chargée ou établie", () => {
    expect(service.estAuthentifie()).toBe(false);
    expect(service.accessToken()).toBeNull();
  });

  it('applique et persiste la session reçue lors de la connexion (RG §4.4)', () => {
    service.connecter('admin@obv-gestion.local', 'motdepasse').subscribe();

    const requete = httpMock.expectOne('/api/v1/auth/login');
    expect(requete.request.method).toBe('POST');
    requete.flush(REPONSE_CONNEXION);

    expect(service.estAuthentifie()).toBe(true);
    expect(service.utilisateurId()).toBe(42);
    expect(service.aLaPermission('VENTE_WRITE')).toBe(true);
    expect(service.aLaPermission('UTILISATEUR_WRITE')).toBe(false);
    expect(stockage.charger()?.accessToken).toBe('jeton-acces');
  });

  it('efface la session locale immédiatement lors de la déconnexion (fire-and-forget)', () => {
    service.connecter('admin@obv-gestion.local', 'motdepasse').subscribe();
    httpMock.expectOne('/api/v1/auth/login').flush(REPONSE_CONNEXION);

    service.deconnecter();

    expect(service.estAuthentifie()).toBe(false);
    expect(service.utilisateurId()).toBeNull();
    expect(stockage.charger()).toBeNull();

    // La requête de logout part bien, mais son échec ne doit rien changer localement.
    const requeteLogout = httpMock.expectOne('/api/v1/auth/logout');
    requeteLogout.flush('erreur', { status: 500, statusText: 'Erreur serveur' });
    expect(service.estAuthentifie()).toBe(false);
  });

  it('recharge une session persistée en localStorage au démarrage (survit à un rechargement de page)', () => {
    stockage.enregistrer({
      accessToken: 'jeton-persiste',
      refreshToken: 'refresh-persiste',
      utilisateurId: 7,
      permissions: ['RAPPORT_READ'],
    });

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const serviceRelance = TestBed.inject(AuthService);

    expect(serviceRelance.estAuthentifie()).toBe(true);
    expect(serviceRelance.utilisateurId()).toBe(7);
    expect(serviceRelance.aLaPermission('RAPPORT_READ')).toBe(true);

    TestBed.inject(HttpTestingController).verify();
  });
});
