import { HttpClient, HttpContext } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Pageable, PageResponse, versParams } from './pagination';
import {
  AffectationRequest,
  CreerUtilisateurRequest,
  ModifierUtilisateurRequest,
  RoleUtilisateur,
  StatutUtilisateur,
  UtilisateurResponse,
} from './models/utilisateur.model';

/** §4.1, §4.3 — création et administration des comptes utilisateurs. */
@Injectable({ providedIn: 'root' })
export class UtilisateurApiService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/utilisateurs';

  lister(
    filtres?: { statut?: StatutUtilisateur; role?: RoleUtilisateur; pdv?: number; recherche?: string },
    pageable?: Pageable,
    context?: HttpContext,
  ): Observable<PageResponse<UtilisateurResponse>> {
    return this.http.get<PageResponse<UtilisateurResponse>>(this.base, {
      params: versParams(pageable, filtres),
      context,
    });
  }

  creer(requete: CreerUtilisateurRequest): Observable<UtilisateurResponse> {
    return this.http.post<UtilisateurResponse>(this.base, requete);
  }

  modifier(id: number, requete: ModifierUtilisateurRequest): Observable<UtilisateurResponse> {
    return this.http.patch<UtilisateurResponse>(`${this.base}/${id}`, requete);
  }

  activer(id: number): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/activer`, {});
  }

  desactiver(id: number): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/desactiver`, {});
  }

  archiver(id: number): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/archiver`, {});
  }

  reinitialiserMotDePasse(id: number): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/reinitialiser-mot-de-passe`, {});
  }

  ajouterAffectation(id: number, requete: AffectationRequest): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/affectations`, requete);
  }

  retirerAffectation(id: number, affectationId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}/affectations/${affectationId}`);
  }
}
