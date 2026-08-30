package com.obvgestion.domain.vente;

/** RG-26 — préfixe de numérotation des documents de vente. */
public enum TypeNumeroDocument {
    BON_COMMANDE("BC"),
    FACTURE("FA");

    private final String prefixe;

    TypeNumeroDocument(String prefixe) {
        this.prefixe = prefixe;
    }

    public String prefixe() {
        return prefixe;
    }
}
