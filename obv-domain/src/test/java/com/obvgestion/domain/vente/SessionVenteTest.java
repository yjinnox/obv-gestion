package com.obvgestion.domain.vente;

import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.referentiel.TypePointDeVente;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessionVenteTest {

    private final PointDeVente depot = new PointDeVente("Dépôt central", TypePointDeVente.DEPOT, "Abidjan");

    private SessionVente nouvelleSessionOuverte() {
        return new SessionVente(depot, "1", new Montant(20000), Instant.now());
    }

    @Test
    void unNouvelleSessionEstOuverte() {
        SessionVente session = nouvelleSessionOuverte();
        assertThat(session.getStatut()).isEqualTo(StatutSessionVente.OUVERTE);
    }

    @Test
    void clotureCalculeLEcartPositifSiSurplus() {
        SessionVente session = nouvelleSessionOuverte();
        session.cloturer("1", new Montant(100000), new Montant(101000), Instant.now());
        assertThat(session.getStatut()).isEqualTo(StatutSessionVente.CLOTUREE);
        assertThat(session.getEcartXof()).isEqualTo(1000);
    }

    @Test
    void clotureCalculeLEcartNegatifSiManquant() {
        SessionVente session = nouvelleSessionOuverte();
        session.cloturer("1", new Montant(100000), new Montant(98000), Instant.now());
        assertThat(session.getEcartXof()).isEqualTo(-2000);
    }

    @Test
    void uneSessionDejaClotureeNePeutPasEtreReClotureee() {
        SessionVente session = nouvelleSessionOuverte();
        session.cloturer("1", Montant.zero(), Montant.zero(), Instant.now());
        assertThatThrownBy(() -> session.cloturer("1", Montant.zero(), Montant.zero(), Instant.now()))
                .isInstanceOf(SessionVenteInvalideException.class);
    }

    /**
     * RG-01/RG-28 — le validateur peut être l'auteur de la clôture : tant
     * que la session n'est pas validée, elle reste rouvrable en
     * modification pour correction.
     */
    @Test
    void validerParLauteurDeLaClotureEstAccepte() {
        SessionVente session = nouvelleSessionOuverte();
        session.cloturer("1", Montant.zero(), Montant.zero(), Instant.now());
        session.valider("1", Instant.now());
        assertThat(session.getStatut()).isEqualTo(StatutSessionVente.VALIDEE);
    }

    @Test
    void validerParUnAutreUtilisateurEstAccepte() {
        SessionVente session = nouvelleSessionOuverte();
        session.cloturer("1", Montant.zero(), Montant.zero(), Instant.now());
        session.valider("2", Instant.now());
        assertThat(session.getStatut()).isEqualTo(StatutSessionVente.VALIDEE);
    }

    @Test
    void uneSessionOuverteNePeutPasEtreValideeDirectement() {
        SessionVente session = nouvelleSessionOuverte();
        assertThatThrownBy(() -> session.valider("2", Instant.now())).isInstanceOf(SessionVenteInvalideException.class);
    }

    /** RG-29 — demande de modification depuis une session clôturée. */
    @Test
    void demanderModificationBasculeEnModification() {
        SessionVente session = nouvelleSessionOuverte();
        session.cloturer("1", Montant.zero(), Montant.zero(), Instant.now());
        session.demanderModification();
        assertThat(session.getStatut()).isEqualTo(StatutSessionVente.EN_MODIFICATION);
    }

    @Test
    void demanderModificationDepuisOuverteEstRefuse() {
        SessionVente session = nouvelleSessionOuverte();
        assertThatThrownBy(session::demanderModification).isInstanceOf(SessionVenteInvalideException.class);
    }

    /** RG-29 — une session rouverte en modification se revalide, y compris par l'auteur de la clôture. */
    @Test
    void validerApresModificationEstAccepte() {
        SessionVente session = nouvelleSessionOuverte();
        session.cloturer("1", Montant.zero(), Montant.zero(), Instant.now());
        session.demanderModification();
        session.valider("1", Instant.now());
        assertThat(session.getStatut()).isEqualTo(StatutSessionVente.VALIDEE);
    }

    @Test
    void exigerOuverteEchoueHorsOuverte() {
        SessionVente session = nouvelleSessionOuverte();
        session.cloturer("1", Montant.zero(), Montant.zero(), Instant.now());
        assertThatThrownBy(session::exigerOuverte).isInstanceOf(SessionVenteInvalideException.class);
    }
}
