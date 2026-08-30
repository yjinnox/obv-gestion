package com.obvgestion.application.rapport;

import java.util.List;

/** §13 — stock valorisé, au coût d'achat (dépôt uniquement, cf. {@link LigneStockValorise}). */
public record RapportStockValorise(List<LigneStockValorise> lignes, long valeurTotaleXof) {

    public RapportStockValorise {
        lignes = List.copyOf(lignes);
    }
}
