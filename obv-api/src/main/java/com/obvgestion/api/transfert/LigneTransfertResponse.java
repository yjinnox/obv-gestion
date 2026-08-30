package com.obvgestion.api.transfert;

import com.obvgestion.domain.transfert.LigneTransfert;

public record LigneTransfertResponse(Long id, Long produitId, String marqueLibelle, String volumeLibelle,
                                      Long conditionnementId, int capaciteBouteilles, long quantiteDemiCasiers,
                                      long quantiteBouteilles, long prixCessionCasierXof, long montantLigneXof) {

    public static LigneTransfertResponse de(LigneTransfert ligne) {
        return new LigneTransfertResponse(
                ligne.getId(), ligne.getProduit().getId(), ligne.getProduit().getMarque().getLibelle(),
                ligne.getProduit().getVolume().getLibelle(), ligne.getConditionnement().getId(),
                ligne.getConditionnement().getCapaciteBouteilles(), ligne.getQuantiteDemiCasiers(),
                ligne.getQuantiteBouteilles(), ligne.getPrixCessionCasier().valeurXof(),
                ligne.montantLigne().valeurXof());
    }
}
