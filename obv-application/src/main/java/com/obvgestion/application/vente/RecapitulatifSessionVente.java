package com.obvgestion.application.vente;

import com.obvgestion.domain.vente.SessionVente;

import java.util.Map;

/** §8.3 — récapitulatif de clôture : quantités vendues par marque/volume, recette par mode de paiement. */
public record RecapitulatifSessionVente(SessionVente session, Map<String, Long> quantiteParMarqueDemiCasiers,
                                         Map<String, Long> quantiteParVolumeDemiCasiers,
                                         long quantiteTotaleDemiCasiers, Map<String, Long> recetteParModePaiementXof,
                                         long recetteTotaleXof) {

    public RecapitulatifSessionVente {
        quantiteParMarqueDemiCasiers = Map.copyOf(quantiteParMarqueDemiCasiers);
        quantiteParVolumeDemiCasiers = Map.copyOf(quantiteParVolumeDemiCasiers);
        recetteParModePaiementXof = Map.copyOf(recetteParModePaiementXof);
    }
}
