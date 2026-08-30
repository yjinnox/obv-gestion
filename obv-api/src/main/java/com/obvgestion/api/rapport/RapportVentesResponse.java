package com.obvgestion.api.rapport;

import com.obvgestion.application.rapport.RapportVentes;

import java.time.Instant;
import java.util.Map;

public record RapportVentesResponse(Long pointDeVenteId, String pointDeVenteLibelle, String pointDeVenteType,
                                     Instant periodeDu, Instant periodeAu, long quantiteTotale,
                                     Map<String, Long> quantiteParMarque, Map<String, Long> quantiteParVolume,
                                     Map<String, Long> recetteParModePaiementXof,
                                     Map<String, Long> quantiteParServeur, Map<String, Long> recetteParJourXof,
                                     long recetteTotaleXof) {

    public RapportVentesResponse {
        quantiteParMarque = Map.copyOf(quantiteParMarque);
        quantiteParVolume = Map.copyOf(quantiteParVolume);
        recetteParModePaiementXof = Map.copyOf(recetteParModePaiementXof);
        quantiteParServeur = Map.copyOf(quantiteParServeur);
        recetteParJourXof = Map.copyOf(recetteParJourXof);
    }

    public static RapportVentesResponse de(RapportVentes rapport) {
        return new RapportVentesResponse(
                rapport.pointDeVenteId(), rapport.pointDeVenteLibelle(), rapport.pointDeVenteType().name(),
                rapport.periodeDu(), rapport.periodeAu(), rapport.quantiteTotale(), rapport.quantiteParMarque(),
                rapport.quantiteParVolume(), rapport.recetteParModePaiementXof(), rapport.quantiteParServeur(),
                rapport.recetteParJourXof(), rapport.recetteTotaleXof());
    }
}
