package com.obvgestion.domain.referentiel;

/**
 * Nature d'un tarif (§5.2) : prix d'achat fournisseur, prix de vente, ou
 * prix de cession interne dépôt → bar (§20.8 — tarif interne, distinct du
 * prix de vente public).
 */
public enum NatureTarif {
    ACHAT,
    VENTE,
    CESSION
}
