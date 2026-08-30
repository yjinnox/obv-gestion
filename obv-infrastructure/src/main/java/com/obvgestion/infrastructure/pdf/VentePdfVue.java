package com.obvgestion.infrastructure.pdf;

import java.util.List;

/**
 * Vue à plat d'une vente pour les gabarits PDF (§8.2, bon de commande et
 * facture). Classe (pas un record) : garantit une résolution de propriété
 * Thymeleaf/OGNL sans ambiguïté, quelle que soit la version.
 */
final class VentePdfVue {

    private final String entrepriseRaisonSociale;
    private final String entrepriseAdresse;
    private final String entrepriseNumeroContribuable;
    private final String numeroDocument;
    private final String dateHeure;
    private final String clientNom;
    private final String clientAdresse;
    private final String pointDeVenteLibelle;
    private final String modePaiement;
    private final List<LigneVentePdfVue> lignes;
    private final long montantSousTotalXof;
    private final int tauxTvaPourcent;
    private final long montantTvaXof;
    private final long montantConsigneXof;
    private final long montantTotalXof;

    VentePdfVue(String entrepriseRaisonSociale, String entrepriseAdresse, String entrepriseNumeroContribuable,
                String numeroDocument, String dateHeure, String clientNom, String clientAdresse,
                String pointDeVenteLibelle, String modePaiement, List<LigneVentePdfVue> lignes,
                long montantSousTotalXof, int tauxTvaPourcent, long montantTvaXof, long montantConsigneXof,
                long montantTotalXof) {
        this.entrepriseRaisonSociale = entrepriseRaisonSociale;
        this.entrepriseAdresse = entrepriseAdresse;
        this.entrepriseNumeroContribuable = entrepriseNumeroContribuable;
        this.numeroDocument = numeroDocument;
        this.dateHeure = dateHeure;
        this.clientNom = clientNom;
        this.clientAdresse = clientAdresse;
        this.pointDeVenteLibelle = pointDeVenteLibelle;
        this.modePaiement = modePaiement;
        this.lignes = List.copyOf(lignes);
        this.montantSousTotalXof = montantSousTotalXof;
        this.tauxTvaPourcent = tauxTvaPourcent;
        this.montantTvaXof = montantTvaXof;
        this.montantConsigneXof = montantConsigneXof;
        this.montantTotalXof = montantTotalXof;
    }

    public String getEntrepriseRaisonSociale() {
        return entrepriseRaisonSociale;
    }

    public String getEntrepriseAdresse() {
        return entrepriseAdresse;
    }

    public String getEntrepriseNumeroContribuable() {
        return entrepriseNumeroContribuable;
    }

    public String getNumeroDocument() {
        return numeroDocument;
    }

    public String getDateHeure() {
        return dateHeure;
    }

    public String getClientNom() {
        return clientNom;
    }

    public String getClientAdresse() {
        return clientAdresse;
    }

    public String getPointDeVenteLibelle() {
        return pointDeVenteLibelle;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public List<LigneVentePdfVue> getLignes() {
        return lignes;
    }

    public long getMontantSousTotalXof() {
        return montantSousTotalXof;
    }

    public int getTauxTvaPourcent() {
        return tauxTvaPourcent;
    }

    public long getMontantTvaXof() {
        return montantTvaXof;
    }

    public long getMontantConsigneXof() {
        return montantConsigneXof;
    }

    public long getMontantTotalXof() {
        return montantTotalXof;
    }
}
