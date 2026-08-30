package com.obvgestion.api.bar;

import com.obvgestion.domain.bar.TicketServeur;

import java.time.Instant;
import java.util.List;

public record TicketServeurResponse(Long id, Long sessionVenteId, Long serveurId, String serveurNom,
                                     String serveurPrenoms, String statut, String modePaiement, long montantTotalXof,
                                     Instant dateEncaissement, String encaisseePar,
                                     List<LigneTicketServeurResponse> lignes) {

    public TicketServeurResponse {
        lignes = List.copyOf(lignes);
    }

    public static TicketServeurResponse de(TicketServeur ticket) {
        return new TicketServeurResponse(
                ticket.getId(), ticket.getSessionVente().getId(), ticket.getServeur().getId(),
                ticket.getServeur().getNom(), ticket.getServeur().getPrenoms(), ticket.getStatut().name(),
                ticket.getModePaiement() == null ? null : ticket.getModePaiement().name(),
                ticket.getMontantTotal().valeurXof(), ticket.getDateEncaissement(), ticket.getEncaisseePar(),
                ticket.getLignes().stream().map(LigneTicketServeurResponse::de).toList());
    }
}
