package com.obvgestion.application.stock;

import com.obvgestion.domain.stock.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/** Port de persistance des soldes de stock, implémenté en infrastructure. */
public interface StockRepository {

    Stock enregistrer(Stock stock);

    Optional<Stock> parPointDeVenteEtProduit(Long pointDeVenteId, Long produitId);

    Page<Stock> rechercher(Long pointDeVenteId, Pageable pageable);
}
