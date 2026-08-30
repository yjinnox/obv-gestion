package com.obvgestion.application.rapport;

import com.obvgestion.domain.referentiel.TypePointDeVente;

import java.time.Instant;
import java.util.Map;

/**
 * §13 — rapport de ventes sur une période, pour un point de vente. Les
 * ventes au dépôt (demi-casiers) et au bar (bouteilles) ne sont pas la même
 * unité (RG-11/RG-12) : un point de vente donné n'alimente jamais qu'un
 * seul des deux couples ({@code recetteParModePaiementXof}) ou
 * ({@code quantiteParServeur}), l'autre restant vide.
 */
public record RapportVentes(Long pointDeVenteId, String pointDeVenteLibelle, TypePointDeVente pointDeVenteType,
                             Instant periodeDu, Instant periodeAu, long quantiteTotale,
                             Map<String, Long> quantiteParMarque, Map<String, Long> quantiteParVolume,
                             Map<String, Long> recetteParModePaiementXof, Map<String, Long> quantiteParServeur,
                             Map<String, Long> recetteParJourXof, long recetteTotaleXof) {

    public RapportVentes {
        quantiteParMarque = Map.copyOf(quantiteParMarque);
        quantiteParVolume = Map.copyOf(quantiteParVolume);
        recetteParModePaiementXof = Map.copyOf(recetteParModePaiementXof);
        quantiteParServeur = Map.copyOf(quantiteParServeur);
        recetteParJourXof = Map.copyOf(recetteParJourXof);
    }
}
