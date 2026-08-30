import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Pageable, PageResponse, versParams } from './pagination';
import {
  AjouterLigneTicketRequest,
  CreerTicketServeurRequest,
  EncaisserTicketRequest,
  ModifierLigneTicketRequest,
  RecapitulatifSessionBarResponse,
  TicketServeurResponse,
} from './models/bar.model';

/** §10 — vente bar par ticket serveur (RG-01, RG-33, RG-34). */
@Injectable({ providedIn: 'root' })
export class TicketServeurApiService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/tickets-serveur';

  lister(
    filtres?: { sessionVenteId?: number; serveurId?: number; statut?: string },
    pageable?: Pageable,
  ): Observable<PageResponse<TicketServeurResponse>> {
    return this.http.get<PageResponse<TicketServeurResponse>>(this.base, { params: versParams(pageable, filtres) });
  }

  obtenir(id: number): Observable<TicketServeurResponse> {
    return this.http.get<TicketServeurResponse>(`${this.base}/${id}`);
  }

  recapitulatif(sessionVenteId: number): Observable<RecapitulatifSessionBarResponse> {
    return this.http.get<RecapitulatifSessionBarResponse>(`${this.base}/recapitulatif`, {
      params: versParams(undefined, { sessionVenteId }),
    });
  }

  creer(requete: CreerTicketServeurRequest): Observable<TicketServeurResponse> {
    return this.http.post<TicketServeurResponse>(this.base, requete);
  }

  ajouterLigne(id: number, requete: AjouterLigneTicketRequest): Observable<TicketServeurResponse> {
    return this.http.post<TicketServeurResponse>(`${this.base}/${id}/lignes`, requete);
  }

  encaisser(id: number, requete: EncaisserTicketRequest): Observable<TicketServeurResponse> {
    return this.http.post<TicketServeurResponse>(`${this.base}/${id}/encaisser`, requete);
  }

  modifierLigne(id: number, ligneId: number, requete: ModifierLigneTicketRequest): Observable<TicketServeurResponse> {
    return this.http.patch<TicketServeurResponse>(`${this.base}/${id}/lignes/${ligneId}`, requete);
  }
}
