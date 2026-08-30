package com.obvgestion.api.vente;

import com.obvgestion.application.vente.RecapitulatifSessionVente;

import java.util.Map;

public record RecapitulatifSessionVenteResponse(SessionVenteResponse session,
                                                 Map<String, Long> quantiteParMarqueDemiCasiers,
                                                 Map<String, Long> quantiteParVolumeDemiCasiers,
                                                 long quantiteTotaleDemiCasiers,
                                                 Map<String, Long> recetteParModePaiementXof, long recetteTotaleXof) {

    public RecapitulatifSessionVenteResponse {
        quantiteParMarqueDemiCasiers = Map.copyOf(quantiteParMarqueDemiCasiers);
        quantiteParVolumeDemiCasiers = Map.copyOf(quantiteParVolumeDemiCasiers);
        recetteParModePaiementXof = Map.copyOf(recetteParModePaiementXof);
    }

    public static RecapitulatifSessionVenteResponse de(RecapitulatifSessionVente recapitulatif) {
        return new RecapitulatifSessionVenteResponse(
                SessionVenteResponse.de(recapitulatif.session()), recapitulatif.quantiteParMarqueDemiCasiers(),
                recapitulatif.quantiteParVolumeDemiCasiers(), recapitulatif.quantiteTotaleDemiCasiers(),
                recapitulatif.recetteParModePaiementXof(), recapitulatif.recetteTotaleXof());
    }
}
