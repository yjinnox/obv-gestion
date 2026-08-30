package com.obvgestion.api.rapport;

import com.obvgestion.application.rapport.LigneStockValorise;
import com.obvgestion.application.rapport.RapportStockValorise;
import com.obvgestion.application.rapport.RapportVentes;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * §13 — export CSV des rapports (délimiteur {@code ;}, BOM UTF-8) : simple
 * mise en forme texte, sans dépendance externe, contrairement aux PDF de
 * vente (§8.2) qui nécessitent un moteur de rendu.
 */
final class RapportCsvWriter {

    private static final String SEPARATEUR = ";";
    private static final byte[] BOM_UTF8 = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private RapportCsvWriter() {
    }

    static byte[] ventes(RapportVentes rapport) {
        StringBuilder texte = new StringBuilder();
        ligne(texte, "Point de vente", rapport.pointDeVenteLibelle());
        ligne(texte, "Période du", texteOuVide(rapport.periodeDu()));
        ligne(texte, "Période au", texteOuVide(rapport.periodeAu()));
        ligne(texte, "Quantité totale", String.valueOf(rapport.quantiteTotale()));
        ligne(texte, "Recette totale (XOF)", String.valueOf(rapport.recetteTotaleXof()));
        texte.append('\n');
        section(texte, "Recette par jour (XOF)", rapport.recetteParJourXof());
        section(texte, "Quantité par marque", rapport.quantiteParMarque());
        section(texte, "Quantité par volume", rapport.quantiteParVolume());
        if (!rapport.recetteParModePaiementXof().isEmpty()) {
            section(texte, "Recette par mode de paiement (XOF)", rapport.recetteParModePaiementXof());
        }
        if (!rapport.quantiteParServeur().isEmpty()) {
            section(texte, "Quantité par serveur", rapport.quantiteParServeur());
        }
        return octets(texte);
    }

    static byte[] stockValorise(RapportStockValorise rapport) {
        StringBuilder texte = new StringBuilder();
        entete(texte, "Point de vente", "Marque", "Volume", "Quantité", "Prix d'achat casier (XOF)",
                "Valeur (XOF)");
        for (LigneStockValorise ligne : rapport.lignes()) {
            entete(texte, ligne.pointDeVenteLibelle(), ligne.marqueLibelle(), ligne.volumeLibelle(),
                    String.valueOf(ligne.quantite()), texteOuVide(ligne.prixAchatCasierXof()),
                    texteOuVide(ligne.valeurLigneXof()));
        }
        texte.append('\n');
        ligne(texte, "Valeur totale (XOF)", String.valueOf(rapport.valeurTotaleXof()));
        return octets(texte);
    }

    private static void section(StringBuilder texte, String titre, Map<String, Long> valeurs) {
        texte.append(champ(titre)).append('\n');
        valeurs.forEach((cle, valeur) -> ligne(texte, cle, String.valueOf(valeur)));
        texte.append('\n');
    }

    private static void ligne(StringBuilder texte, String cle, String valeur) {
        entete(texte, cle, valeur);
    }

    private static void entete(StringBuilder texte, String... champs) {
        for (int i = 0; i < champs.length; i++) {
            if (i > 0) {
                texte.append(SEPARATEUR);
            }
            texte.append(champ(champs[i]));
        }
        texte.append('\n');
    }

    private static String texteOuVide(Object valeur) {
        return valeur == null ? "" : String.valueOf(valeur);
    }

    private static String champ(String valeur) {
        if (valeur == null) {
            return "";
        }
        if (valeur.contains(SEPARATEUR) || valeur.contains("\"") || valeur.contains("\n")) {
            return "\"" + valeur.replace("\"", "\"\"") + "\"";
        }
        return valeur;
    }

    private static byte[] octets(StringBuilder texte) {
        byte[] contenu = texte.toString().getBytes(StandardCharsets.UTF_8);
        byte[] resultat = new byte[BOM_UTF8.length + contenu.length];
        System.arraycopy(BOM_UTF8, 0, resultat, 0, BOM_UTF8.length);
        System.arraycopy(contenu, 0, resultat, BOM_UTF8.length, contenu.length);
        return resultat;
    }
}
