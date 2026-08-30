package com.obvgestion.application.stock;

import com.obvgestion.domain.stock.MouvementStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

/** Port de persistance du journal des mouvements de stock (append-only). */
public interface MouvementStockRepository {

    MouvementStock enregistrer(MouvementStock mouvement);

    Page<MouvementStock> rechercher(Long pointDeVenteId, Long produitId, Instant du, Instant au, Pageable pageable);
}
