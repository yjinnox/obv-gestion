package com.obvgestion.domain.stock;

import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.referentiel.Produit;

/** Un mouvement à appliquer au sein d'un groupe atomique (RG-31, voir le port applicatif de stock). */
public record MouvementDemande(PointDeVente pointDeVente, Produit produit, TypeMouvementStock type,
                                long quantiteSignee) {
}
