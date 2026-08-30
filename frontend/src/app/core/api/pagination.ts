import { HttpParams } from '@angular/common/http';

/** Enveloppe de pagination Spring Data (voir PageResponse<T> côté API, §13). */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Pageable {
  page?: number;
  size?: number;
  sort?: string | string[];
}

/** Ajoute page/size/sort (+ params métier additionnels) à une requête GET. */
export function versParams(pageable?: Pageable, filtres?: Record<string, unknown>): HttpParams {
  let params = new HttpParams();
  if (pageable?.page !== undefined) {
    params = params.set('page', pageable.page);
  }
  if (pageable?.size !== undefined) {
    params = params.set('size', pageable.size);
  }
  if (pageable?.sort) {
    for (const tri of Array.isArray(pageable.sort) ? pageable.sort : [pageable.sort]) {
      params = params.append('sort', tri);
    }
  }
  if (filtres) {
    for (const [cle, valeur] of Object.entries(filtres)) {
      if (valeur !== undefined && valeur !== null && valeur !== '') {
        params = params.set(cle, String(valeur));
      }
    }
  }
  return params;
}
