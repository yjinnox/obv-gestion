import { HttpClient, HttpContext } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Pageable, PageResponse, versParams } from './pagination';
import {
  ClotureSessionVenteRequest,
  OuvrirSessionVenteRequest,
  RecapitulatifSessionVenteResponse,
  SessionVenteResponse,
} from './models/vente.model';

/** §8.1/§8.3 — session de vente (journée de caisse) d'un point de vente. */
@Injectable({ providedIn: 'root' })
export class SessionVenteApiService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/sessions-vente';

  lister(filtres?: { pdv?: number }, pageable?: Pageable): Observable<PageResponse<SessionVenteResponse>> {
    return this.http.get<PageResponse<SessionVenteResponse>>(this.base, { params: versParams(pageable, filtres) });
  }

  obtenir(id: number): Observable<SessionVenteResponse> {
    return this.http.get<SessionVenteResponse>(`${this.base}/${id}`);
  }

  courante(pdv: number, context?: HttpContext): Observable<SessionVenteResponse> {
    return this.http.get<SessionVenteResponse>(`${this.base}/courante`, {
      params: versParams(undefined, { pdv }),
      context,
    });
  }

  recapitulatif(id: number): Observable<RecapitulatifSessionVenteResponse> {
    return this.http.get<RecapitulatifSessionVenteResponse>(`${this.base}/${id}/recapitulatif`);
  }

  ouvrir(requete: OuvrirSessionVenteRequest): Observable<SessionVenteResponse> {
    return this.http.post<SessionVenteResponse>(`${this.base}/ouvrir`, requete);
  }

  cloturer(id: number, requete: ClotureSessionVenteRequest): Observable<SessionVenteResponse> {
    return this.http.post<SessionVenteResponse>(`${this.base}/${id}/cloturer`, requete);
  }

  valider(id: number): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/valider`, {});
  }

  demanderModification(id: number): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/demander-modification`, {});
  }
}
