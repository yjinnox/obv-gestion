import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Pageable, PageResponse, versParams } from './pagination';
import { MouvementStockResponse, StockResponse } from './models/stock.model';

/** §6 — soldes de stock et journal des mouvements (lecture seule). */
@Injectable({ providedIn: 'root' })
export class StockApiService {
  private readonly http = inject(HttpClient);

  lister(filtres?: { pdv?: number }, pageable?: Pageable): Observable<PageResponse<StockResponse>> {
    return this.http.get<PageResponse<StockResponse>>('/api/v1/stocks', { params: versParams(pageable, filtres) });
  }

  listerMouvements(
    filtres?: { pdv?: number; produit?: number; du?: string; au?: string },
    pageable?: Pageable,
  ): Observable<PageResponse<MouvementStockResponse>> {
    return this.http.get<PageResponse<MouvementStockResponse>>('/api/v1/mouvements-stock', {
      params: versParams(pageable, filtres),
    });
  }
}
