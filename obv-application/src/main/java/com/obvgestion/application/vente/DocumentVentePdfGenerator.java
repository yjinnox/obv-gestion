package com.obvgestion.application.vente;

import com.obvgestion.domain.vente.Vente;

/** §8.2 étape 6 — génération PDF du bon de commande et de la facture, implémentée en infrastructure. */
public interface DocumentVentePdfGenerator {

    byte[] genererBonDeCommande(Vente vente);

    byte[] genererFacture(Vente vente);
}
