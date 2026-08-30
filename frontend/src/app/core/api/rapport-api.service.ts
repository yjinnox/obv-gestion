import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { versParams } from './pagination';
import { RapportStockValoriseResponse, RapportVentesResponse } from './models/rapport.model';

/** §7 (P7) — rapports de ventes et de stock valorisé, avec export CSV. */
@Injectable({ providedIn: 'root' })
export class RapportApiService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/rapports';

  ventes(filtres: { pdv: number; du?: string; au?: string }): Observable<RapportVentesResponse> {
    return this.http.get<RapportVentesResponse>(`${this.base}/ventes`, { params: versParams(undefined, filtres) });
  }

  exporterVentesCsv(filtres: { pdv: number; du?: string; au?: string }): Observable<Blob> {
    return this.http.get(`${this.base}/ventes/export.csv`, {
      params: versParams(undefined, filtres),
      responseType: 'blob',
    });
  }

  stockValorise(filtres?: { pdv?: number }): Observable<RapportStockValoriseResponse> {
    return this.http.get<RapportStockValoriseResponse>(`${this.base}/stock-valorise`, {
      params: versParams(undefined, filtres),
    });
  }

  exporterStockValoriseCsv(filtres?: { pdv?: number }): Observable<Blob> {
    return this.http.get(`${this.base}/stock-valorise/export.csv`, {
      params: versParams(undefined, filtres),
      responseType: 'blob',
    });
  }
}
