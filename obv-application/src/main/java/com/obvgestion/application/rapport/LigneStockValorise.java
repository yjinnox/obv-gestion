package com.obvgestion.application.rapport;

/**
 * Une ligne du stock valorisé (§13). {@code prixAchatCasierXof}/{@code valeurLigneXof}
 * sont {@code null} quand la ligne n'est pas valorisable : stock du bar
 * (aucun coût par bouteille tracé, seul le prix de cession du casier
 * transféré l'est — non historisé par bouteille) ou produit du dépôt sans
 * tarif ACHAT en vigueur.
 */
public record LigneStockValorise(Long pointDeVenteId, String pointDeVenteLibelle, Long produitId,
                                  String marqueLibelle, String volumeLibelle, long quantite, Long prixAchatCasierXof,
                                  Long valeurLigneXof) {
}
