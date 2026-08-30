package com.obvgestion.application.vente;

import java.util.List;

/** §8.2 étape 3 — écran panier : lignes (produit, volume, nb casiers, PU, total ligne) + montant global. */
public record PanierDetaille(Long utilisateurId, Long sessionVenteId, List<LignePanierDetaillee> lignes,
                              long montantGlobalXof) {

    public PanierDetaille {
        lignes = List.copyOf(lignes);
    }
}
