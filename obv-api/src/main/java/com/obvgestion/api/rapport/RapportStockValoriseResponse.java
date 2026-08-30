package com.obvgestion.api.rapport;

import com.obvgestion.application.rapport.RapportStockValorise;

import java.util.List;

public record RapportStockValoriseResponse(List<LigneStockValoriseResponse> lignes, long valeurTotaleXof) {

    public RapportStockValoriseResponse {
        lignes = List.copyOf(lignes);
    }

    public static RapportStockValoriseResponse de(RapportStockValorise rapport) {
        return new RapportStockValoriseResponse(
                rapport.lignes().stream().map(LigneStockValoriseResponse::de).toList(), rapport.valeurTotaleXof());
    }
}
