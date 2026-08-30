package com.obvgestion.api.reception;

import com.obvgestion.application.reception.RecapitulatifReception;
import com.obvgestion.domain.commun.Montant;

import java.util.Map;
import java.util.stream.Collectors;

/** §7.2 étape 3 — total par marque, par volume, montant total. */
public record RecapitulatifReceptionResponse(ReceptionResponse reception, Map<String, Long> totalParMarqueXof,
                                              Map<String, Long> totalParVolumeXof, long montantTotalXof) {

    public RecapitulatifReceptionResponse {
        totalParMarqueXof = Map.copyOf(totalParMarqueXof);
        totalParVolumeXof = Map.copyOf(totalParVolumeXof);
    }

    public static RecapitulatifReceptionResponse de(RecapitulatifReception recapitulatif) {
        return new RecapitulatifReceptionResponse(
                ReceptionResponse.de(recapitulatif.reception()),
                versXof(recapitulatif.totalParMarque()),
                versXof(recapitulatif.totalParVolume()),
                recapitulatif.montantTotal().valeurXof());
    }

    private static Map<String, Long> versXof(Map<String, Montant> totaux) {
        return totaux.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entree -> entree.getValue().valeurXof()));
    }
}
