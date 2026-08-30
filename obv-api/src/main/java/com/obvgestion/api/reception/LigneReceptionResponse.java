package com.obvgestion.api.reception;

import com.obvgestion.domain.reception.LigneReception;

public record LigneReceptionResponse(Long id, Long produitId, String marqueLibelle, String volumeLibelle,
                                      Long conditionnementId, int capaciteBouteilles, long nombreCasiers,
                                      long prixAchatCasierXof, long montantLigneXof) {

    public static LigneReceptionResponse de(LigneReception ligne) {
        return new LigneReceptionResponse(
                ligne.getId(), ligne.getProduit().getId(), ligne.getProduit().getMarque().getLibelle(),
                ligne.getProduit().getVolume().getLibelle(), ligne.getConditionnement().getId(),
                ligne.getConditionnement().getCapaciteBouteilles(), ligne.getNombreCasiers(),
                ligne.getPrixAchatCasier().valeurXof(), ligne.montantLigne().valeurXof());
    }
}
