package com.obvgestion.api.referentiel;

import com.obvgestion.domain.referentiel.Fournisseur;

public record FournisseurResponse(Long id, String raisonSociale, String telephone, String email, String adresse,
                                   boolean actif) {

    public static FournisseurResponse de(Fournisseur fournisseur) {
        return new FournisseurResponse(
                fournisseur.getId(), fournisseur.getRaisonSociale(), fournisseur.getTelephone(),
                fournisseur.getEmail(), fournisseur.getAdresse(), fournisseur.isActif());
    }
}
