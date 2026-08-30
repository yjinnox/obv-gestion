package com.obvgestion.api.vente;

import com.obvgestion.domain.vente.LigneVente;

public record LigneVenteResponse(Long id, Long produitId, String marqueLibelle, String volumeLibelle,
                                  long quantiteDemiCasiers, long prixVenteCasierXof, long montantConsigneCasierXof,
                                  long montantLigneXof) {

    public static LigneVenteResponse de(LigneVente ligne) {
        return new LigneVenteResponse(
                ligne.getId(), ligne.getProduit().getId(), ligne.getProduit().getMarque().getLibelle(),
                ligne.getProduit().getVolume().getLibelle(), ligne.quantiteDemiCasiers(),
                ligne.getPrixVenteCasier().valeurXof(), ligne.getMontantConsigneCasier().valeurXof(),
                ligne.montantLigne().valeurXof());
    }
}
