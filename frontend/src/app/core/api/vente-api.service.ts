import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Pageable, PageResponse, versParams } from './pagination';
import { CreerCommandeRequest, ModifierQuantiteLigneVenteRequest, VenteResponse } from './models/vente.model';

/** §8.2 — commandes (ventes) issues de la validation d'un panier. */
@Injectable({ providedIn: 'root' })
export class VenteApiService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/commandes';

  lister(filtres?: { sessionVenteId?: number }, pageable?: Pageable): Observable<PageResponse<VenteResponse>> {
    return this.http.get<PageResponse<VenteResponse>>(this.base, { params: versParams(pageable, filtres) });
  }

  obtenir(id: number): Observable<VenteResponse> {
    return this.http.get<VenteResponse>(`${this.base}/${id}`);
  }

  /** RG-27 — idempotente : une nouvelle clé est générée à chaque appel du formulaire. */
  commander(requete: CreerCommandeRequest): Observable<VenteResponse> {
    return this.http.post<VenteResponse>(this.base, requete, {
      headers: { 'Idempotency-Key': crypto.randomUUID() },
    });
  }

  modifierQuantiteLigne(
    id: number,
    ligneId: number,
    requete: ModifierQuantiteLigneVenteRequest,
  ): Observable<VenteResponse> {
    return this.http.patch<VenteResponse>(`${this.base}/${id}/lignes/${ligneId}`, requete);
  }

  telechargerBonDeCommande(id: number): Observable<Blob> {
    return this.http.get(`${this.base}/${id}/bon-de-commande.pdf`, { responseType: 'blob' });
  }

  telechargerFacture(id: number): Observable<Blob> {
    return this.http.get(`${this.base}/${id}/facture.pdf`, { responseType: 'blob' });
  }
}
