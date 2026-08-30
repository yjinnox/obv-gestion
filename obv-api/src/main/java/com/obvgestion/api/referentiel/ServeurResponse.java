package com.obvgestion.api.referentiel;

import com.obvgestion.domain.referentiel.Serveur;

public record ServeurResponse(Long id, Long pointDeVenteId, String nom, String prenoms, String telephone,
                               boolean actif) {

    public static ServeurResponse de(Serveur serveur) {
        return new ServeurResponse(
                serveur.getId(), serveur.getPointDeVente().getId(), serveur.getNom(), serveur.getPrenoms(),
                serveur.getTelephone(), serveur.isActif());
    }
}
