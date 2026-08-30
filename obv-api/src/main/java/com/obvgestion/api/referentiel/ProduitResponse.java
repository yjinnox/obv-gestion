package com.obvgestion.api.referentiel;

import com.obvgestion.domain.referentiel.Produit;

public record ProduitResponse(Long id, Long marqueId, String marqueLibelle, Long volumeId, String volumeLibelle,
                               boolean actif) {

    public static ProduitResponse de(Produit produit) {
        return new ProduitResponse(
                produit.getId(),
                produit.getMarque().getId(), produit.getMarque().getLibelle(),
                produit.getVolume().getId(), produit.getVolume().getLibelle(),
                produit.isActif());
    }
}
