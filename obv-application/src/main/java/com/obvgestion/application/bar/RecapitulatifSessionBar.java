package com.obvgestion.application.bar;

import com.obvgestion.domain.vente.SessionVente;

import java.util.Map;

/** RG-33 — récapitulatif de clôture du bar : total par serveur, par marque/volume, total général. */
public record RecapitulatifSessionBar(SessionVente session, Map<String, Long> quantiteParServeurBouteilles,
                                       Map<String, Long> quantiteParMarqueBouteilles,
                                       Map<String, Long> quantiteParVolumeBouteilles, long quantiteTotaleBouteilles,
                                       long recetteTotaleXof) {

    public RecapitulatifSessionBar {
        quantiteParServeurBouteilles = Map.copyOf(quantiteParServeurBouteilles);
        quantiteParMarqueBouteilles = Map.copyOf(quantiteParMarqueBouteilles);
        quantiteParVolumeBouteilles = Map.copyOf(quantiteParVolumeBouteilles);
    }
}
