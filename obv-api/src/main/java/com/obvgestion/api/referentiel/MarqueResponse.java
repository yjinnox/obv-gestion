package com.obvgestion.api.referentiel;

import com.obvgestion.domain.referentiel.Marque;

public record MarqueResponse(Long id, String libelle, boolean actif) {

    public static MarqueResponse de(Marque marque) {
        return new MarqueResponse(marque.getId(), marque.getLibelle(), marque.isActif());
    }
}
