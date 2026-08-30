package com.obvgestion.domain.bar;

import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.referentiel.Marque;
import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.referentiel.Produit;
import com.obvgestion.domain.referentiel.Serveur;
import com.obvgestion.domain.referentiel.TypePointDeVente;
import com.obvgestion.domain.referentiel.Volume;
import com.obvgestion.domain.vente.ModePaiement;
import com.obvgestion.domain.vente.SessionVente;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicketServeurTest {

    private final PointDeVente bar = new PointDeVente("Le Maquis", TypePointDeVente.BAR, "Abidjan");
    private final PointDeVente autreBar = new PointDeVente("Chez Tantie", TypePointDeVente.BAR, "Abidjan");
    private final Serveur serveur = new Serveur(bar, "Kouadio", "Awa", "0102030405");
    private final Serveur serveurAutreBar = new Serveur(autreBar, "Yao", "Paul", "0102030406");
    private final Produit produit = new Produit(new Marque("Flag"), new Volume("33 cl", 330));

    private SessionVente sessionOuverte() {
        return new SessionVente(bar, "gerant@obv.ci", new Montant(15000), Instant.now());
    }

    private TicketServeur nouveauTicketAvecLigne() {
        TicketServeur ticket = new TicketServeur(sessionOuverte(), serveur);
        ticket.ajouterLigne(produit, 5, new Montant(1000));
        return ticket;
    }

    @Test
    void unTicketNePeutEtreOuvertQueDansUneSessionOuverte() {
        SessionVente session = sessionOuverte();
        session.cloturer("gerant@obv.ci", Montant.zero(), Montant.zero(), Instant.now());
        assertThatThrownBy(() -> new TicketServeur(session, serveur))
                .isInstanceOf(com.obvgestion.domain.vente.SessionVenteInvalideException.class);
    }

    @Test
    void leServeurDoitAppartenirAuPointDeVenteDeLaSession() {
        assertThatThrownBy(() -> new TicketServeur(sessionOuverte(), serveurAutreBar))
                .isInstanceOf(TicketServeurInvalideException.class);
    }

    @Test
    void ajouterUneLigneEstRefuseeAvecUneQuantiteNulle() {
        TicketServeur ticket = new TicketServeur(sessionOuverte(), serveur);
        assertThatThrownBy(() -> ticket.ajouterLigne(produit, 0, new Montant(1000)))
                .isInstanceOf(TicketServeurInvalideException.class);
    }

    @Test
    void unTicketSansLigneNePeutPasEtreEncaisse() {
        TicketServeur ticket = new TicketServeur(sessionOuverte(), serveur);
        assertThatThrownBy(() -> ticket.encaisser(ModePaiement.ESPECES, "gerant@obv.ci", Instant.now()))
                .isInstanceOf(TicketServeurInvalideException.class);
    }

    @Test
    void encaisserCalculeLeMontantTotalEtBasculeEnEncaisse() {
        TicketServeur ticket = nouveauTicketAvecLigne();
        ticket.encaisser(ModePaiement.ESPECES, "gerant@obv.ci", Instant.now());
        assertThat(ticket.getStatut()).isEqualTo(StatutTicketServeur.ENCAISSE);
        assertThat(ticket.getMontantTotal()).isEqualTo(new Montant(5 * 1000));
        assertThat(ticket.getModePaiement()).isEqualTo(ModePaiement.ESPECES);
        assertThat(ticket.getEncaisseePar()).isEqualTo("gerant@obv.ci");
    }

    @Test
    void unTicketDejaEncaisseNePeutPasEtreReEncaisse() {
        TicketServeur ticket = nouveauTicketAvecLigne();
        ticket.encaisser(ModePaiement.ESPECES, "gerant@obv.ci", Instant.now());
        assertThatThrownBy(() -> ticket.encaisser(ModePaiement.ESPECES, "gerant@obv.ci", Instant.now()))
                .isInstanceOf(TicketServeurInvalideException.class);
    }

    @Test
    void unTicketEncaisseNePeutPlusRecevoirDeLigne() {
        TicketServeur ticket = nouveauTicketAvecLigne();
        ticket.encaisser(ModePaiement.ESPECES, "gerant@obv.ci", Instant.now());
        assertThatThrownBy(() -> ticket.ajouterLigne(produit, 1, new Montant(1000)))
                .isInstanceOf(TicketServeurInvalideException.class);
    }

    @Test
    void plusieursLignesSommentLeMontantTotal() {
        TicketServeur ticket = new TicketServeur(sessionOuverte(), serveur);
        ticket.ajouterLigne(produit, 5, new Montant(1000));
        ticket.ajouterLigne(produit, 2, new Montant(1200));
        ticket.encaisser(ModePaiement.MOBILE_MONEY, "gerant@obv.ci", Instant.now());
        assertThat(ticket.getMontantTotal()).isEqualTo(new Montant(5 * 1000 + 2 * 1200));
    }
}
