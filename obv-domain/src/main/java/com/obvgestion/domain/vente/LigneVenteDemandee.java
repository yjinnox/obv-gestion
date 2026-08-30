package com.obvgestion.domain.vente;

import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.referentiel.Produit;

/**
 * Ligne demandée à la commande (§8.2 étape 6), issue du panier ; les prix
 * sont déjà résolus par l'appelant. {@code quantiteDemiCasiers} : unité
 * canonique RG-11 (1 = demi-casier, 2 = un casier).
 */
public record LigneVenteDemandee(Produit produit, long quantiteDemiCasiers, Montant prixVenteCasier,
                                  Montant montantConsigneCasier) {
}
