package com.obvgestion.domain.reception;

import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.commun.SeparationDesTachesException;
import com.obvgestion.domain.referentiel.Conditionnement;
import com.obvgestion.domain.referentiel.Fournisseur;
import com.obvgestion.domain.referentiel.Marque;
import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.referentiel.Produit;
import com.obvgestion.domain.referentiel.TypePointDeVente;
import com.obvgestion.domain.referentiel.Volume;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReceptionTest {

    private final Fournisseur fournisseur = new Fournisseur("SOBEBRA", "0102030405", null, null);
    private final PointDeVente depot = new PointDeVente("Dépôt central", TypePointDeVente.DEPOT, "Abidjan");
    private final PointDeVente bar = new PointDeVente("Le Maquis", TypePointDeVente.BAR, "Abidjan");
    private final Produit produit = new Produit(new Marque("Flag"), new Volume("33 cl", 330));
    private final Conditionnement conditionnement = new Conditionnement(produit, 24);

    private Reception nouvelleReceptionAvecLigne() {
        Reception reception = new Reception(fournisseur, depot, Instant.now());
        reception.ajouterLigne(produit, conditionnement, 10, new Montant(8500));
        return reception;
    }

    @Test
    void uneReceptionNePeutEtreCreeeQuePourUnDepot() {
        assertThatThrownBy(() -> new Reception(fournisseur, bar, Instant.now()))
                .isInstanceOf(ReceptionInvalideException.class);
    }

    @Test
    void ajouterUneLigneConvertitLesCasiersEnDemiCasiers() {
        Reception reception = nouvelleReceptionAvecLigne();
        assertThat(reception.getLignes()).singleElement()
                .satisfies(ligne -> assertThat(ligne.quantiteDemiCasiers()).isEqualTo(20));
    }

    @Test
    void uneReceptionSansLigneNePeutPasEtreCloturee() {
        Reception reception = new Reception(fournisseur, depot, Instant.now());
        assertThatThrownBy(() -> reception.cloturer("gerant@obv.ci")).isInstanceOf(ReceptionInvalideException.class);
    }

    @Test
    void clotureBasculeEnAttenteDeValidation() {
        Reception reception = nouvelleReceptionAvecLigne();
        reception.cloturer("gerant@obv.ci");
        assertThat(reception.getStatut()).isEqualTo(StatutReception.EN_ATTENTE_VALIDATION);
        assertThat(reception.getClotureePar()).isEqualTo("gerant@obv.ci");
    }

    @Test
    void uneReceptionDejaClotureeNePeutPasEtreReClotureee() {
        Reception reception = nouvelleReceptionAvecLigne();
        reception.cloturer("gerant@obv.ci");
        assertThatThrownBy(() -> reception.cloturer("gerant@obv.ci")).isInstanceOf(ReceptionInvalideException.class);
    }

    /** RG-01 — un utilisateur ne peut jamais valider un document qu'il a lui-même clôturé. */
    @Test
    void validerParLauteurDeLaClotureEstRefuse() {
        Reception reception = nouvelleReceptionAvecLigne();
        reception.cloturer("gerant@obv.ci");
        assertThatThrownBy(() -> reception.valider("gerant@obv.ci")).isInstanceOf(SeparationDesTachesException.class);
    }

    @Test
    void validerParUnAutreUtilisateurEstAccepte() {
        Reception reception = nouvelleReceptionAvecLigne();
        reception.cloturer("gerant@obv.ci");
        reception.valider("super-admin@obv.ci");
        assertThat(reception.getStatut()).isEqualTo(StatutReception.VALIDEE);
    }

    @Test
    void unBrouillonNePeutPasEtreValideDirectement() {
        Reception reception = nouvelleReceptionAvecLigne();
        assertThatThrownBy(() -> reception.valider("super-admin@obv.ci"))
                .isInstanceOf(ReceptionInvalideException.class);
    }

    /** RG-22 — le motif d'annulation est obligatoire. */
    @Test
    void annulerSansMotifEstRefuse() {
        Reception reception = nouvelleReceptionAvecLigne();
        reception.cloturer("gerant@obv.ci");
        assertThatThrownBy(() -> reception.annuler(" ")).isInstanceOf(ReceptionInvalideException.class);
    }

    /** RG-18 — l'annulation est logique : la réception passe à ANNULEE avec son motif tracé. */
    @Test
    void annulerAvecMotifPasseAAnnulee() {
        Reception reception = nouvelleReceptionAvecLigne();
        reception.cloturer("gerant@obv.ci");
        reception.annuler("Erreur de saisie du fournisseur");
        assertThat(reception.getStatut()).isEqualTo(StatutReception.ANNULEE);
        assertThat(reception.getMotifAnnulation()).isEqualTo("Erreur de saisie du fournisseur");
    }

    /** RG-21 — une réception validée est immuable. */
    @Test
    void uneReceptionValideeNePeutPlusEtreModifiee() {
        Reception reception = nouvelleReceptionAvecLigne();
        reception.cloturer("gerant@obv.ci");
        reception.valider("super-admin@obv.ci");
        assertThatThrownBy(() -> reception.ajouterLigne(produit, conditionnement, 1, new Montant(8500)))
                .isInstanceOf(ReceptionInvalideException.class);
    }

    @Test
    void supprimerUneLigneHorsBrouillonEstRefuse() {
        Reception reception = nouvelleReceptionAvecLigne();
        Long ligneId = reception.getLignes().get(0).getId();
        reception.cloturer("gerant@obv.ci");
        assertThatThrownBy(() -> reception.supprimerLigne(ligneId)).isInstanceOf(ReceptionInvalideException.class);
    }

    @Test
    void montantTotalSommeLesLignes() {
        Reception reception = new Reception(fournisseur, depot, Instant.now());
        reception.ajouterLigne(produit, conditionnement, 10, new Montant(8500));
        reception.ajouterLigne(produit, conditionnement, 5, new Montant(9000));
        assertThat(reception.montantTotal()).isEqualTo(new Montant(10 * 8500 + 5 * 9000));
    }
}
