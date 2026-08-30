import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Pageable, PageResponse, versParams } from './pagination';
import { AnnulerTransfertRequest, BonTransfertResponse, CreerBonTransfertRequest } from './models/transfert.model';

/** §9 — cycle de vie complet d'un transfert dépôt → bar. */
@Injectable({ providedIn: 'root' })
export class TransfertApiService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/transferts';

  lister(
    filtres?: { source?: number; destination?: number; statut?: string },
    pageable?: Pageable,
  ): Observable<PageResponse<BonTransfertResponse>> {
    return this.http.get<PageResponse<BonTransfertResponse>>(this.base, { params: versParams(pageable, filtres) });
  }

  obtenir(id: number): Observable<BonTransfertResponse> {
    return this.http.get<BonTransfertResponse>(`${this.base}/${id}`);
  }

  creer(requete: CreerBonTransfertRequest): Observable<BonTransfertResponse> {
    return this.http.post<BonTransfertResponse>(this.base, requete);
  }

  cloturer(id: number): Observable<BonTransfertResponse> {
    return this.http.post<BonTransfertResponse>(`${this.base}/${id}/cloturer`, {});
  }

  valider(id: number): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/valider`, {});
  }

  annuler(id: number, requete: AnnulerTransfertRequest): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/annuler`, requete);
  }
}
