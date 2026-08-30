package com.obvgestion.api.referentiel;

import com.obvgestion.domain.referentiel.Conditionnement;

public record ConditionnementResponse(Long id, Long produitId, int capaciteBouteilles, boolean demiCasierAutorise,
                                       boolean actif) {

    public static ConditionnementResponse de(Conditionnement conditionnement) {
        return new ConditionnementResponse(
                conditionnement.getId(), conditionnement.getProduit().getId(),
                conditionnement.getCapaciteBouteilles(), conditionnement.isDemiCasierAutorise(),
                conditionnement.isActif());
    }
}
