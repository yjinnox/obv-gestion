package com.obvgestion.application.transfert;

/**
 * §9 — ligne demandée à la création d'un transfert. {@code prixCessionCasierXof}
 * nullable : pré-rempli depuis le tarif CESSION en vigueur si omis.
 */
public record LigneTransfertDemandee(Long produitId, Long conditionnementId, long quantiteDemiCasiers,
                                      Long prixCessionCasierXof) {
}
