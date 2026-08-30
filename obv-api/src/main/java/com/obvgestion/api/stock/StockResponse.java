package com.obvgestion.api.stock;

import com.obvgestion.domain.stock.Stock;

public record StockResponse(Long id, Long pointDeVenteId, Long produitId, String marqueLibelle,
                             String volumeLibelle, long quantite) {

    public static StockResponse de(Stock stock) {
        return new StockResponse(
                stock.getId(), stock.getPointDeVente().getId(), stock.getProduit().getId(),
                stock.getProduit().getMarque().getLibelle(), stock.getProduit().getVolume().getLibelle(),
                stock.getQuantite());
    }
}
