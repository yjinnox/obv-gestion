import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { creerCrudApi } from './crud-api';
import { Pageable, PageResponse, versParams } from './pagination';
import {
  ClientResponse,
  ConditionnementResponse,
  CreerClientRequest,
  CreerConditionnementRequest,
  CreerFournisseurRequest,
  CreerMarqueRequest,
  CreerPointDeVenteRequest,
  CreerProduitRequest,
  CreerServeurRequest,
  CreerTarifRequest,
  CreerVolumeRequest,
  FournisseurResponse,
  MarqueResponse,
  ModifierClientRequest,
  ModifierConditionnementRequest,
  ModifierFournisseurRequest,
  ModifierMarqueRequest,
  ModifierPointDeVenteRequest,
  ModifierProduitRequest,
  ModifierServeurRequest,
  ModifierVolumeRequest,
  PointDeVenteResponse,
  ProduitResponse,
  ServeurResponse,
  TarifResponse,
  VolumeResponse,
} from './models/referentiel.model';

/**
 * §5 — référentiel (catalogue, points de vente, tiers). Toutes les
 * entités CRUD simples partagent {@link creerCrudApi} ; Tarif (historisé,
 * jamais modifié/supprimé — RG-08/RG-09) reste à part.
 */
@Injectable({ providedIn: 'root' })
export class ReferentielApiService {
  private readonly http = inject(HttpClient);

  readonly marques = creerCrudApi<MarqueResponse, CreerMarqueRequest, ModifierMarqueRequest>(
    this.http,
    '/api/v1/marques',
  );
  readonly volumes = creerCrudApi<VolumeResponse, CreerVolumeRequest, ModifierVolumeRequest>(
    this.http,
    '/api/v1/volumes',
  );
  readonly produits = creerCrudApi<ProduitResponse, CreerProduitRequest, ModifierProduitRequest>(
    this.http,
    '/api/v1/produits',
  );
  readonly conditionnements = creerCrudApi<
    ConditionnementResponse,
    CreerConditionnementRequest,
    ModifierConditionnementRequest
  >(this.http, '/api/v1/conditionnements');
  readonly pointsDeVente = creerCrudApi<
    PointDeVenteResponse,
    CreerPointDeVenteRequest,
    ModifierPointDeVenteRequest
  >(this.http, '/api/v1/points-de-vente');
  readonly fournisseurs = creerCrudApi<FournisseurResponse, CreerFournisseurRequest, ModifierFournisseurRequest>(
    this.http,
    '/api/v1/fournisseurs',
  );
  readonly clients = creerCrudApi<ClientResponse, CreerClientRequest, ModifierClientRequest>(
    this.http,
    '/api/v1/clients',
  );
  readonly serveurs = creerCrudApi<ServeurResponse, CreerServeurRequest, ModifierServeurRequest>(
    this.http,
    '/api/v1/serveurs',
  );

  listerTarifs(
    filtres?: { pdv?: number; produit?: number; nature?: string },
    pageable?: Pageable,
  ): Observable<PageResponse<TarifResponse>> {
    return this.http.get<PageResponse<TarifResponse>>('/api/v1/tarifs', { params: versParams(pageable, filtres) });
  }

  creerTarif(requete: CreerTarifRequest): Observable<TarifResponse> {
    return this.http.post<TarifResponse>('/api/v1/tarifs', requete);
  }
}
