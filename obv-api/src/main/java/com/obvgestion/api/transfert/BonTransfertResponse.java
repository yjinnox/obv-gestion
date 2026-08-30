package com.obvgestion.api.transfert;

import com.obvgestion.domain.transfert.BonTransfert;

import java.time.Instant;
import java.util.List;

public record BonTransfertResponse(Long id, String numero, Long pointDeVenteSourceId,
                                    String pointDeVenteSourceLibelle, Long pointDeVenteDestinationId,
                                    String pointDeVenteDestinationLibelle, Instant dateHeure, String statut,
                                    String motifAnnulation, String clotureePar, List<LigneTransfertResponse> lignes,
                                    long montantTotalXof) {

    public BonTransfertResponse {
        lignes = List.copyOf(lignes);
    }

    public static BonTransfertResponse de(BonTransfert transfert) {
        return new BonTransfertResponse(
                transfert.getId(), transfert.getNumero(), transfert.getPointDeVenteSource().getId(),
                transfert.getPointDeVenteSource().getLibelle(), transfert.getPointDeVenteDestination().getId(),
                transfert.getPointDeVenteDestination().getLibelle(), transfert.getDateHeure(),
                transfert.getStatut().name(), transfert.getMotifAnnulation(), transfert.getClotureePar(),
                transfert.getLignes().stream().map(LigneTransfertResponse::de).toList(),
                transfert.montantTotal().valeurXof());
    }
}
