package com.obvgestion.api.vente;

import com.obvgestion.application.vente.ResultatAjoutPanier;

/** RG-24 — {@code stockDisponibleDemiCasiers} est informatif, jamais bloquant à l'ajout au panier. */
public record AjoutPanierResponse(long stockDisponibleDemiCasiers) {

    public static AjoutPanierResponse de(ResultatAjoutPanier resultat) {
        return new AjoutPanierResponse(resultat.stockDisponibleDemiCasiers());
    }
}
