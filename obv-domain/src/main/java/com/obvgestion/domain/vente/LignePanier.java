package com.obvgestion.domain.vente;

/**
 * Ligne d'un panier (§8.2) : identifiant local au panier, pas de prix figé
 * tant que non commandée. RG-11 : {@code quantiteDemiCasiers} est l'unité
 * canonique (1 = demi-casier, 2 = un casier).
 */
public record LignePanier(int id, Long produitId, long quantiteDemiCasiers) {
}
