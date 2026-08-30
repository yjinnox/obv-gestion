package com.obvgestion.api.referentiel;

import com.obvgestion.domain.referentiel.NatureTarif;
import com.obvgestion.domain.referentiel.UniteVente;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/** RG-08 — la création clôt automatiquement le tarif ouvert pour cette clé, le cas échéant. */
public record CreerTarifRequest(@NotNull Long pointDeVenteId, @NotNull Long produitId,
                                 @NotNull UniteVente uniteVente, @NotNull NatureTarif nature,
                                 @Positive long montantXof, @NotNull LocalDate dateDebut) {
}
