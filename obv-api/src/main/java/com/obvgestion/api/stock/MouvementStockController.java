package com.obvgestion.api.stock;

import com.obvgestion.api.PageResponse;
import com.obvgestion.application.stock.StockService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/** §6.2, §13 — journal des mouvements de stock. */
@RestController
@RequestMapping("/api/v1/mouvements-stock")
class MouvementStockController {

    private final StockService service;

    MouvementStockController(StockService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('RECEPTION_READ', 'RECEPTION_WRITE')")
    PageResponse<MouvementStockResponse> lister(@RequestParam(name = "pdv", required = false) Long pointDeVenteId,
                                                 @RequestParam(name = "produit", required = false) Long produitId,
                                                 @RequestParam(required = false) Instant du,
                                                 @RequestParam(required = false) Instant au,
                                                 Pageable pageable) {
        return PageResponse.de(
                service.rechercherMouvements(pointDeVenteId, produitId, du, au, pageable), MouvementStockResponse::de);
    }
}
