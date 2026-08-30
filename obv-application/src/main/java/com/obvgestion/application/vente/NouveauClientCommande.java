package com.obvgestion.application.vente;

import com.obvgestion.domain.referentiel.TypeClient;

/** §8.2 étape 5 — création d'un client « à la volée » lors de la commande. */
public record NouveauClientCommande(TypeClient type, String nom, String prenoms, String raisonSociale,
                                     String telephone, String email, String adresseFacturation) {
}
