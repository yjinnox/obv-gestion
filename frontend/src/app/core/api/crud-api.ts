import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Pageable, PageResponse, versParams } from './pagination';

/**
 * CRUD générique pour les entités du référentiel qui suivent toutes le
 * même contrat (`GET`/`GET {id}`/`POST`/`PATCH {id}`/`DELETE {id}`,
 * suppression = désactivation logique). Évite de dupliquer neuf fois la
 * même mécanique HTTP (marques, volumes, produits, conditionnements,
 * points de vente, fournisseurs, clients, serveurs).
 */
export function creerCrudApi<TResponse, TCreate, TModifier>(http: HttpClient, cheminBase: string) {
  return {
    lister(filtres?: Record<string, unknown>, pageable?: Pageable): Observable<PageResponse<TResponse>> {
      return http.get<PageResponse<TResponse>>(cheminBase, { params: versParams(pageable, filtres) });
    },
    obtenir(id: number): Observable<TResponse> {
      return http.get<TResponse>(`${cheminBase}/${id}`);
    },
    creer(requete: TCreate): Observable<TResponse> {
      return http.post<TResponse>(cheminBase, requete);
    },
    modifier(id: number, requete: TModifier): Observable<TResponse> {
      return http.patch<TResponse>(`${cheminBase}/${id}`, requete);
    },
    supprimer(id: number): Observable<void> {
      return http.delete<void>(`${cheminBase}/${id}`);
    },
  };
}
