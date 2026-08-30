package com.obvgestion.api.referentiel;

import com.obvgestion.domain.referentiel.PointDeVente;

public record PointDeVenteResponse(Long id, String libelle, String type, String adresse, boolean actif) {

    public static PointDeVenteResponse de(PointDeVente pointDeVente) {
        return new PointDeVenteResponse(
                pointDeVente.getId(), pointDeVente.getLibelle(), pointDeVente.getType().name(),
                pointDeVente.getAdresse(), pointDeVente.isActif());
    }
}
