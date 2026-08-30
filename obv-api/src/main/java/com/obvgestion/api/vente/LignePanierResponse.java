package com.obvgestion.api.vente;

import com.obvgestion.application.vente.LignePanierDetaillee;

public record LignePanierResponse(int id, Long produitId, String marqueLibelle, String volumeLibelle,
                                   long quantiteDemiCasiers, long prixVenteCasierXof, long montantLigneXof) {

    public static LignePanierResponse de(LignePanierDetaillee ligne) {
        return new LignePanierResponse(ligne.id(), ligne.produitId(), ligne.marqueLibelle(), ligne.volumeLibelle(),
                ligne.quantiteDemiCasiers(), ligne.prixVenteCasierXof(), ligne.montantLigneXof());
    }
}
