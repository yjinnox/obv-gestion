package com.obvgestion.api.reception;

import com.obvgestion.domain.reception.Reception;

import java.time.Instant;
import java.util.List;

public record ReceptionResponse(Long id, Long fournisseurId, String fournisseurRaisonSociale, Long pointDeVenteId,
                                 String pointDeVenteLibelle, Instant dateHeureLivraison, String statut,
                                 String motifAnnulation, String clotureePar, List<LigneReceptionResponse> lignes,
                                 long montantTotalXof) {

    public ReceptionResponse {
        lignes = List.copyOf(lignes);
    }

    public static ReceptionResponse de(Reception reception) {
        return new ReceptionResponse(
                reception.getId(), reception.getFournisseur().getId(), reception.getFournisseur().getRaisonSociale(),
                reception.getPointDeVente().getId(), reception.getPointDeVente().getLibelle(),
                reception.getDateHeureLivraison(), reception.getStatut().name(), reception.getMotifAnnulation(),
                reception.getClotureePar(), reception.getLignes().stream().map(LigneReceptionResponse::de).toList(),
                reception.montantTotal().valeurXof());
    }
}
