package com.obvgestion.application.reception;

import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.reception.Reception;

import java.util.Map;

/** §7.2 étape 3 — récapitulatif de clôture : total par marque, par volume, montant total. */
public record RecapitulatifReception(Reception reception, Map<String, Montant> totalParMarque,
                                      Map<String, Montant> totalParVolume, Montant montantTotal) {

    public RecapitulatifReception {
        totalParMarque = Map.copyOf(totalParMarque);
        totalParVolume = Map.copyOf(totalParVolume);
    }
}
