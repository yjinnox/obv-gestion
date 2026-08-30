package com.obvgestion.domain.vente;

import java.util.ArrayList;
import java.util.List;

/**
 * Panier de vente au dépôt (§8.2), persisté en Redis (TTL 4h, clé =
 * utilisateur + session) — pas une entité JPA, jamais historisé : RG-25,
 * la suppression du panier n'a aucun effet sur le stock ni sur la base.
 */
public record Panier(Long utilisateurId, Long sessionVenteId, List<LignePanier> lignes) {

    public Panier {
        lignes = List.copyOf(lignes);
    }

    public static Panier vide(Long utilisateurId, Long sessionVenteId) {
        return new Panier(utilisateurId, sessionVenteId, List.of());
    }

    public Panier avecLigneAjoutee(Long produitId, long quantiteDemiCasiers) {
        int prochainId = lignes.stream().mapToInt(LignePanier::id).max().orElse(0) + 1;
        List<LignePanier> nouvellesLignes = new ArrayList<>(lignes);
        nouvellesLignes.add(new LignePanier(prochainId, produitId, quantiteDemiCasiers));
        return new Panier(utilisateurId, sessionVenteId, nouvellesLignes);
    }

    public Panier avecLigneModifiee(int ligneId, long quantiteDemiCasiers) {
        List<LignePanier> nouvellesLignes = lignes.stream()
                .map(ligne -> ligne.id() == ligneId
                        ? new LignePanier(ligne.id(), ligne.produitId(), quantiteDemiCasiers) : ligne)
                .toList();
        return new Panier(utilisateurId, sessionVenteId, nouvellesLignes);
    }

    public Panier sansLigne(int ligneId) {
        return new Panier(utilisateurId, sessionVenteId,
                lignes.stream().filter(ligne -> ligne.id() != ligneId).toList());
    }
}
