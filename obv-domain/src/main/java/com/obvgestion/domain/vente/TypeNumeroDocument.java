package com.obvgestion.domain.vente;

/**
 * RG-26 — préfixe de numérotation des documents de vente. {@code TRANSFERT}
 * réutilise le même compteur (§9) pour le bon de transfert dépôt → bar.
 */
public enum TypeNumeroDocument {
    BON_COMMANDE("BC"),
    FACTURE("FA"),
    TRANSFERT("BT");

    private final String prefixe;

    TypeNumeroDocument(String prefixe) {
        this.prefixe = prefixe;
    }

    public String prefixe() {
        return prefixe;
    }
}
