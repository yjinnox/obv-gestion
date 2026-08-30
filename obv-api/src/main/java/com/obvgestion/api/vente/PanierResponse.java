package com.obvgestion.api.vente;

import com.obvgestion.application.vente.PanierDetaille;

import java.util.List;

public record PanierResponse(Long utilisateurId, Long sessionVenteId, List<LignePanierResponse> lignes,
                              long montantGlobalXof) {

    public PanierResponse {
        lignes = List.copyOf(lignes);
    }

    public static PanierResponse de(PanierDetaille panier) {
        return new PanierResponse(
                panier.utilisateurId(), panier.sessionVenteId(),
                panier.lignes().stream().map(LignePanierResponse::de).toList(), panier.montantGlobalXof());
    }
}
