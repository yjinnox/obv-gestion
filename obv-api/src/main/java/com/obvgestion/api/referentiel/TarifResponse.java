package com.obvgestion.api.referentiel;

import com.obvgestion.domain.referentiel.Tarif;

import java.time.LocalDate;

public record TarifResponse(Long id, Long pointDeVenteId, Long produitId, String uniteVente, String nature,
                             long montantXof, LocalDate dateDebut, LocalDate dateFin) {

    public static TarifResponse de(Tarif tarif) {
        return new TarifResponse(
                tarif.getId(), tarif.getPointDeVente().getId(), tarif.getProduit().getId(),
                tarif.getUniteVente().name(), tarif.getNature().name(), tarif.getMontant().valeurXof(),
                tarif.getDateDebut(), tarif.getDateFin());
    }
}
