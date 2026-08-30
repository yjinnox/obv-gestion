import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { versParams } from './pagination';
import { AjoutPanierResponse, AjouterLignePanierRequest, ModifierLignePanierRequest, PanierResponse } from './models/vente.model';

/** §8.2 — panier de vente dépôt (stocké en Redis, par session de vente). */
@Injectable({ providedIn: 'root' })
export class PanierApiService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/panier';

  obtenir(sessionVenteId: number): Observable<PanierResponse> {
    return this.http.get<PanierResponse>(this.base, { params: versParams(undefined, { sessionVenteId }) });
  }

  ajouterLigne(requete: AjouterLignePanierRequest): Observable<AjoutPanierResponse> {
    return this.http.post<AjoutPanierResponse>(`${this.base}/lignes`, requete);
  }

  modifierLigne(ligneId: number, requete: ModifierLignePanierRequest): Observable<PanierResponse> {
    return this.http.patch<PanierResponse>(`${this.base}/lignes/${ligneId}`, requete);
  }

  supprimerLigne(ligneId: number, sessionVenteId: number): Observable<PanierResponse> {
    return this.http.delete<PanierResponse>(`${this.base}/lignes/${ligneId}`, {
      params: versParams(undefined, { sessionVenteId }),
    });
  }

  vider(sessionVenteId: number): Observable<void> {
    return this.http.delete<void>(this.base, { params: versParams(undefined, { sessionVenteId }) });
  }
}
