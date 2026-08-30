package com.obvgestion.api.stock;

import com.obvgestion.domain.stock.MouvementStock;

import java.time.Instant;

public record MouvementStockResponse(Long id, Long pointDeVenteId, Long produitId, String marqueLibelle,
                                      String volumeLibelle, String type, long quantiteSignee, long stockAvant,
                                      long stockApres, String documentType, Long documentId, Instant dateHeure,
                                      Long utilisateurId) {

    public static MouvementStockResponse de(MouvementStock mouvement) {
        return new MouvementStockResponse(
                mouvement.getId(), mouvement.getPointDeVente().getId(), mouvement.getProduit().getId(),
                mouvement.getProduit().getMarque().getLibelle(), mouvement.getProduit().getVolume().getLibelle(),
                mouvement.getType().name(), mouvement.getQuantiteSignee(), mouvement.getStockAvant(),
                mouvement.getStockApres(), mouvement.getDocumentType(), mouvement.getDocumentId(),
                mouvement.getDateHeure(), mouvement.getUtilisateur().getId());
    }
}
