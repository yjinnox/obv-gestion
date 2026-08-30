import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Pageable, PageResponse, versParams } from './pagination';
import {
  AjouterLigneReceptionRequest,
  AnnulerReceptionRequest,
  CreerReceptionRequest,
  DemanderValidationRequest,
  ModifierLigneReceptionRequest,
  RecapitulatifReceptionResponse,
  ReceptionResponse,
} from './models/reception.model';

/** §7 — cycle de vie complet d'une réception dépôt. */
@Injectable({ providedIn: 'root' })
export class ReceptionApiService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/receptions';

  lister(
    filtres?: { pdv?: number; statut?: string },
    pageable?: Pageable,
  ): Observable<PageResponse<ReceptionResponse>> {
    return this.http.get<PageResponse<ReceptionResponse>>(this.base, { params: versParams(pageable, filtres) });
  }

  obtenir(id: number): Observable<ReceptionResponse> {
    return this.http.get<ReceptionResponse>(`${this.base}/${id}`);
  }

  recapitulatif(id: number): Observable<RecapitulatifReceptionResponse> {
    return this.http.get<RecapitulatifReceptionResponse>(`${this.base}/${id}/recapitulatif`);
  }

  creer(requete: CreerReceptionRequest): Observable<ReceptionResponse> {
    return this.http.post<ReceptionResponse>(this.base, requete);
  }

  ajouterLigne(id: number, requete: AjouterLigneReceptionRequest): Observable<ReceptionResponse> {
    return this.http.post<ReceptionResponse>(`${this.base}/${id}/lignes`, requete);
  }

  modifierLigne(id: number, ligneId: number, requete: ModifierLigneReceptionRequest): Observable<ReceptionResponse> {
    return this.http.patch<ReceptionResponse>(`${this.base}/${id}/lignes/${ligneId}`, requete);
  }

  supprimerLigne(id: number, ligneId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}/lignes/${ligneId}`);
  }

  cloturer(id: number): Observable<ReceptionResponse> {
    return this.http.post<ReceptionResponse>(`${this.base}/${id}/cloturer`, {});
  }

  demanderValidation(id: number, requete: DemanderValidationRequest): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/demander-validation`, requete);
  }

  valider(id: number): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/valider`, {});
  }

  annuler(id: number, requete: AnnulerReceptionRequest): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/annuler`, requete);
  }
}
