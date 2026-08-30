package com.obvgestion.api.bar;

import com.obvgestion.domain.bar.LigneTicketServeur;

public record LigneTicketServeurResponse(Long id, Long produitId, String marqueLibelle, String volumeLibelle,
                                          long quantiteBouteilles, long prixVenteBouteilleXof, long montantLigneXof) {

    public static LigneTicketServeurResponse de(LigneTicketServeur ligne) {
        return new LigneTicketServeurResponse(
                ligne.getId(), ligne.getProduit().getId(), ligne.getProduit().getMarque().getLibelle(),
                ligne.getProduit().getVolume().getLibelle(), ligne.getQuantiteBouteilles(),
                ligne.getPrixVenteBouteille().valeurXof(), ligne.montantLigne().valeurXof());
    }
}
