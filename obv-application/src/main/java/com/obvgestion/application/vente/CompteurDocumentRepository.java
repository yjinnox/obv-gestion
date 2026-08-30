package com.obvgestion.application.vente;

import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.vente.TypeNumeroDocument;

/** RG-26 — port de numérotation des documents de vente (compteur verrouillé, aucun trou toléré). */
public interface CompteurDocumentRepository {

    /** Incrémente (sous verrou pessimiste, dans la transaction de l'appelant) et retourne le numéro formaté. */
    String prochainNumero(PointDeVente pointDeVente, TypeNumeroDocument type, int annee);
}
