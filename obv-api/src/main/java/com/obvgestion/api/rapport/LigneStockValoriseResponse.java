package com.obvgestion.api.rapport;

import com.obvgestion.application.rapport.LigneStockValorise;

public record LigneStockValoriseResponse(Long pointDeVenteId, String pointDeVenteLibelle, Long produitId,
                                          String marqueLibelle, String volumeLibelle, long quantite,
                                          Long prixAchatCasierXof, Long valeurLigneXof) {

    public static LigneStockValoriseResponse de(LigneStockValorise ligne) {
        return new LigneStockValoriseResponse(
                ligne.pointDeVenteId(), ligne.pointDeVenteLibelle(), ligne.produitId(), ligne.marqueLibelle(),
                ligne.volumeLibelle(), ligne.quantite(), ligne.prixAchatCasierXof(), ligne.valeurLigneXof());
    }
}
