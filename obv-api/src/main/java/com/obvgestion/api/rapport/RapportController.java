package com.obvgestion.api.rapport;

import com.obvgestion.application.rapport.RapportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

/** §13 — rapport de ventes sur une période et stock valorisé (phase P7). */
@RestController
@RequestMapping("/api/v1/rapports")
class RapportController {

    private static final MediaType CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final RapportService service;

    RapportController(RapportService service) {
        this.service = service;
    }

    @GetMapping("/ventes")
    @PreAuthorize("hasAuthority('RAPPORT_READ')")
    RapportVentesResponse ventes(@RequestParam("pdv") Long pointDeVenteId,
                                 @RequestParam(required = false) Instant du, @RequestParam(required = false) Instant au) {
        return RapportVentesResponse.de(service.rapportVentes(pointDeVenteId, du, au));
    }

    @GetMapping("/ventes/export.csv")
    @PreAuthorize("hasAuthority('RAPPORT_READ')")
    ResponseEntity<byte[]> ventesCsv(@RequestParam("pdv") Long pointDeVenteId,
                                      @RequestParam(required = false) Instant du,
                                      @RequestParam(required = false) Instant au) {
        return csv(RapportCsvWriter.ventes(service.rapportVentes(pointDeVenteId, du, au)), "rapport-ventes.csv");
    }

    @GetMapping("/stock-valorise")
    @PreAuthorize("hasAuthority('RAPPORT_READ')")
    RapportStockValoriseResponse stockValorise(@RequestParam(name = "pdv", required = false) Long pointDeVenteId) {
        return RapportStockValoriseResponse.de(service.rapportStockValorise(pointDeVenteId));
    }

    @GetMapping("/stock-valorise/export.csv")
    @PreAuthorize("hasAuthority('RAPPORT_READ')")
    ResponseEntity<byte[]> stockValoriseCsv(@RequestParam(name = "pdv", required = false) Long pointDeVenteId) {
        return csv(RapportCsvWriter.stockValorise(service.rapportStockValorise(pointDeVenteId)),
                "rapport-stock-valorise.csv");
    }

    private static ResponseEntity<byte[]> csv(byte[] contenu, String nomFichier) {
        return ResponseEntity.ok()
                .contentType(CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomFichier + "\"")
                .body(contenu);
    }
}
