package com.obvgestion.api.bar;

import com.obvgestion.api.vente.SessionVenteResponse;
import com.obvgestion.application.bar.RecapitulatifSessionBar;

import java.util.Map;

public record RecapitulatifSessionBarResponse(SessionVenteResponse session,
                                               Map<String, Long> quantiteParServeurBouteilles,
                                               Map<String, Long> quantiteParMarqueBouteilles,
                                               Map<String, Long> quantiteParVolumeBouteilles,
                                               long quantiteTotaleBouteilles, long recetteTotaleXof) {

    public RecapitulatifSessionBarResponse {
        quantiteParServeurBouteilles = Map.copyOf(quantiteParServeurBouteilles);
        quantiteParMarqueBouteilles = Map.copyOf(quantiteParMarqueBouteilles);
        quantiteParVolumeBouteilles = Map.copyOf(quantiteParVolumeBouteilles);
    }

    public static RecapitulatifSessionBarResponse de(RecapitulatifSessionBar recapitulatif) {
        return new RecapitulatifSessionBarResponse(
                SessionVenteResponse.de(recapitulatif.session()), recapitulatif.quantiteParServeurBouteilles(),
                recapitulatif.quantiteParMarqueBouteilles(), recapitulatif.quantiteParVolumeBouteilles(),
                recapitulatif.quantiteTotaleBouteilles(), recapitulatif.recetteTotaleXof());
    }
}
