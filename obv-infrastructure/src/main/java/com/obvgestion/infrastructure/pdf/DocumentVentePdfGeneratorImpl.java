package com.obvgestion.infrastructure.pdf;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.obvgestion.application.vente.DocumentVentePdfGenerator;
import com.obvgestion.domain.referentiel.Client;
import com.obvgestion.domain.referentiel.TypeClient;
import com.obvgestion.domain.vente.LigneVente;
import com.obvgestion.domain.vente.Vente;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** §8.2 étape 6 — génération PDF du bon de commande et de la facture (mêmes gabarits Thymeleaf que les e-mails). */
@Component
public class DocumentVentePdfGeneratorImpl implements DocumentVentePdfGenerator {

    private static final DateTimeFormatter FORMAT_DATE_HEURE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRENCH);

    private final TemplateEngine templateEngine;
    private final String entrepriseRaisonSociale;
    private final String entrepriseAdresse;
    private final String entrepriseNumeroContribuable;
    private final int tauxTvaPourcent;
    private final ZoneId fuseauHoraireMetier;

    public DocumentVentePdfGeneratorImpl(TemplateEngine templateEngine,
                                          @Value("${entreprise.raison-sociale}") String entrepriseRaisonSociale,
                                          @Value("${entreprise.adresse}") String entrepriseAdresse,
                                          @Value("${entreprise.numero-contribuable}") String entrepriseNumeroContribuable,
                                          @Value("${entreprise.taux-tva-pourcent}") int tauxTvaPourcent,
                                          @Value("${app.timezone}") String fuseauHoraireMetier) {
        this.templateEngine = templateEngine;
        this.entrepriseRaisonSociale = entrepriseRaisonSociale;
        this.entrepriseAdresse = entrepriseAdresse;
        this.entrepriseNumeroContribuable = entrepriseNumeroContribuable;
        this.tauxTvaPourcent = tauxTvaPourcent;
        this.fuseauHoraireMetier = ZoneId.of(fuseauHoraireMetier);
    }

    @Override
    public byte[] genererBonDeCommande(Vente vente) {
        return genererPdf("pdf/bon-de-commande", vente, vente.getNumeroBonCommande());
    }

    @Override
    public byte[] genererFacture(Vente vente) {
        return genererPdf("pdf/facture", vente, vente.getNumeroFacture());
    }

    private byte[] genererPdf(String gabarit, Vente vente, String numeroDocument) {
        Context contexte = new Context();
        contexte.setVariable("v", versVue(vente, numeroDocument));
        String html = templateEngine.process(gabarit, contexte);

        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        PdfRendererBuilder constructeur = new PdfRendererBuilder()
                .useFastMode()
                .withHtmlContent(html, null)
                .toStream(sortie);
        try {
            constructeur.run();
        } catch (IOException e) {
            throw new IllegalStateException("Échec de génération du PDF pour " + numeroDocument, e);
        }
        return sortie.toByteArray();
    }

    private VentePdfVue versVue(Vente vente, String numeroDocument) {
        List<LigneVentePdfVue> lignes = vente.getLignes().stream()
                .map(this::versLigneVue)
                .toList();
        return new VentePdfVue(entrepriseRaisonSociale, entrepriseAdresse, entrepriseNumeroContribuable,
                numeroDocument, vente.getDateHeure().atZone(fuseauHoraireMetier).format(FORMAT_DATE_HEURE),
                nomClient(vente.getClient()), vente.getClient().getAdresseFacturation(),
                vente.getSessionVente().getPointDeVente().getLibelle(), vente.getModePaiement().name(), lignes,
                vente.getMontantSousTotal().valeurXof(), tauxTvaPourcent, vente.getMontantTva().valeurXof(),
                vente.getMontantConsigne().valeurXof(), vente.getMontantTotal().valeurXof());
    }

    private LigneVentePdfVue versLigneVue(LigneVente ligne) {
        return new LigneVentePdfVue(ligne.getProduit().getMarque().getLibelle(),
                ligne.getProduit().getVolume().getLibelle(), ligne.quantiteDemiCasiers(),
                ligne.getPrixVenteCasier().valeurXof(), ligne.montantLigne().valeurXof());
    }

    private static String nomClient(Client client) {
        return client.getType() == TypeClient.ENTREPRISE
                ? client.getRaisonSociale() : client.getNom() + " " + client.getPrenoms();
    }
}
