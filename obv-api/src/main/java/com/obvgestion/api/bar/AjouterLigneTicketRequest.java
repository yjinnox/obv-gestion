package com.obvgestion.api.bar;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** {@code prixVenteBouteilleXof} nullable : pré-rempli depuis le tarif VENTE en vigueur si omis (§10). */
public record AjouterLigneTicketRequest(@NotNull Long produitId, @Positive long quantiteBouteilles,
                                         Long prixVenteBouteilleXof) {
}
