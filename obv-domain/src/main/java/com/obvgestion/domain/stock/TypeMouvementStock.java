package com.obvgestion.domain.stock;

/** Nature d'un mouvement de stock (§6.2). */
public enum TypeMouvementStock {
    ENTREE_RECEPTION,
    SORTIE_VENTE,
    SORTIE_TRANSFERT,
    ENTREE_TRANSFERT,
    AJUSTEMENT,
    CONTRE_PASSATION
}
