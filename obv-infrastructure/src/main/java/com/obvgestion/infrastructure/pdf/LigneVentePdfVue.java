package com.obvgestion.infrastructure.pdf;

/**
 * Vue à plat d'une ligne de vente pour les gabarits PDF (§8.2). Classe
 * (pas un record) : garantit une résolution de propriété Thymeleaf/OGNL
 * sans ambiguïté, quelle que soit la version.
 */
final class LigneVentePdfVue {

    private final String marqueLibelle;
    private final String volumeLibelle;
    private final long quantiteDemiCasiers;
    private final long prixVenteCasierXof;
    private final long montantLigneXof;

    LigneVentePdfVue(String marqueLibelle, String volumeLibelle, long quantiteDemiCasiers, long prixVenteCasierXof,
                      long montantLigneXof) {
        this.marqueLibelle = marqueLibelle;
        this.volumeLibelle = volumeLibelle;
        this.quantiteDemiCasiers = quantiteDemiCasiers;
        this.prixVenteCasierXof = prixVenteCasierXof;
        this.montantLigneXof = montantLigneXof;
    }

    public String getMarqueLibelle() {
        return marqueLibelle;
    }

    public String getVolumeLibelle() {
        return volumeLibelle;
    }

    public long getQuantiteDemiCasiers() {
        return quantiteDemiCasiers;
    }

    public long getPrixVenteCasierXof() {
        return prixVenteCasierXof;
    }

    public long getMontantLigneXof() {
        return montantLigneXof;
    }
}
