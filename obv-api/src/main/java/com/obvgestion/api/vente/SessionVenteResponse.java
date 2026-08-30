package com.obvgestion.api.vente;

import com.obvgestion.domain.vente.SessionVente;

import java.time.Instant;

public record SessionVenteResponse(Long id, Long pointDeVenteId, String pointDeVenteLibelle, Instant dateOuverture,
                                    String ouvertePar, long fondCaisseXof, String statut, Instant dateCloture,
                                    String clotureePar, Long totalTheoriqueXof, Long totalCompteXof, Long ecartXof,
                                    Instant dateValidation, String valideePar) {

    public static SessionVenteResponse de(SessionVente session) {
        return new SessionVenteResponse(
                session.getId(), session.getPointDeVente().getId(), session.getPointDeVente().getLibelle(),
                session.getDateOuverture(), session.getOuvertePar(), session.getFondCaisse().valeurXof(),
                session.getStatut().name(), session.getDateCloture(), session.getClotureePar(),
                session.getTotalTheorique() == null ? null : session.getTotalTheorique().valeurXof(),
                session.getTotalCompte() == null ? null : session.getTotalCompte().valeurXof(),
                session.getEcartXof(), session.getDateValidation(), session.getValideePar());
    }
}
