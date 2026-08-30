package com.obvgestion.api.stock;

import com.obvgestion.api.PageResponse;
import com.obvgestion.application.stock.StockService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** §6, §13 — soldes de stock par point de vente. */
@RestController
@RequestMapping("/api/v1/stocks")
class StockController {

    private final StockService service;

    StockController(StockService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('RECEPTION_READ', 'RECEPTION_WRITE')")
    PageResponse<StockResponse> lister(@RequestParam(name = "pdv", required = false) Long pointDeVenteId,
                                        Pageable pageable) {
        return PageResponse.de(service.rechercher(pointDeVenteId, pageable), StockResponse::de);
    }
}
