package com.obvgestion.domain.transfert;

import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.commun.SeparationDesTachesException;
import com.obvgestion.domain.referentiel.Conditionnement;
import com.obvgestion.domain.referentiel.Marque;
import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.referentiel.Produit;
import com.obvgestion.domain.referentiel.TypePointDeVente;
import com.obvgestion.domain.referentiel.Volume;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BonTransfertTest {

    private final PointDeVente depot = new PointDeVente("Dépôt central", TypePointDeVente.DEPOT, "Abidjan");
    private final PointDeVente bar = new PointDeVente("Le Maquis", TypePointDeVente.BAR, "Abidjan");
    private final Produit produit = new Produit(new Marque("Flag"), new Volume("33 cl", 330));
    private final Conditionnement conditionnementPair = new Conditionnement(produit, 24);
    private final Conditionnement conditionnementImpair = new Conditionnement(produit, 15);

    private BonTransfert nouveauTransfertAvecLigne() {
        BonTransfert transfert = new BonTransfert("BT-1-2026-000001", depot, bar, Instant.now());
        transfert.ajouterLigne(produit, conditionnementPair, 6, new Montant(8000));
        return transfert;
    }

    @Test
    void laSourceDoitEtreUnDepot() {
        assertThatThrownBy(() -> new BonTransfert("BT-1-2026-000001", bar, bar, Instant.now()))
                .isInstanceOf(TransfertInvalideException.class);
    }

    @Test
    void laDestinationDoitEtreUnBar() {
        assertThatThrownBy(() -> new BonTransfert("BT-1-2026-000001", depot, depot, Instant.now()))
                .isInstanceOf(TransfertInvalideException.class);
    }

    /** RG-30 — quantiteBouteilles = (quantiteDemiCasiers × capaciteBouteilles) / 2. */
    @Test
    void ajouterUneLigneCalculeLaQuantiteEnBouteilles() {
        BonTransfert transfert = nouveauTransfertAvecLigne();
        assertThat(transfert.getLignes()).singleElement()
                .satisfies(ligne -> assertThat(ligne.getQuantiteBouteilles()).isEqualTo(6 * 24 / 2));
    }

    /** RG-30 — un transfert dont le résultat n'est pas un nombre entier de bouteilles est refusé. */
    @Test
    void unNombreDeBouteillesNonEntierEstRefuse() {
        BonTransfert transfert = new BonTransfert("BT-1-2026-000001", depot, bar, Instant.now());
        assertThatThrownBy(() -> transfert.ajouterLigne(produit, conditionnementImpair, 1, new Montant(8000)))
                .isInstanceOf(TransfertInvalideException.class);
    }

    /** RG-13 (par le calcul RG-30) — un conditionnement à capacité impaire accepte les quantités paires. */
    @Test
    void unConditionnementACapaciteImpaireAccepteUneQuantitePaire() {
        BonTransfert transfert = new BonTransfert("BT-1-2026-000001", depot, bar, Instant.now());
        transfert.ajouterLigne(produit, conditionnementImpair, 2, new Montant(8000));
        assertThat(transfert.getLignes()).singleElement()
                .satisfies(ligne -> assertThat(ligne.getQuantiteBouteilles()).isEqualTo(2 * 15 / 2));
    }

    @Test
    void unTransfertSansLigneNePeutPasEtreCloture() {
        BonTransfert transfert = new BonTransfert("BT-1-2026-000001", depot, bar, Instant.now());
        assertThatThrownBy(() -> transfert.cloturer("gerant@obv.ci")).isInstanceOf(TransfertInvalideException.class);
    }

    @Test
    void clotureBasculeEnAttenteDeValidation() {
        BonTransfert transfert = nouveauTransfertAvecLigne();
        transfert.cloturer("gerant@obv.ci");
        assertThat(transfert.getStatut()).isEqualTo(StatutTransfert.EN_ATTENTE_VALIDATION);
        assertThat(transfert.getClotureePar()).isEqualTo("gerant@obv.ci");
    }

    /** RG-01 — un utilisateur ne peut jamais valider un document qu'il a lui-même clôturé. */
    @Test
    void validerParLauteurDeLaClotureEstRefuse() {
        BonTransfert transfert = nouveauTransfertAvecLigne();
        transfert.cloturer("gerant@obv.ci");
        assertThatThrownBy(() -> transfert.valider("gerant@obv.ci")).isInstanceOf(SeparationDesTachesException.class);
    }

    @Test
    void validerParUnAutreUtilisateurEstAccepte() {
        BonTransfert transfert = nouveauTransfertAvecLigne();
        transfert.cloturer("gerant@obv.ci");
        transfert.valider("super-admin@obv.ci");
        assertThat(transfert.getStatut()).isEqualTo(StatutTransfert.VALIDEE);
    }

    @Test
    void unBrouillonNePeutPasEtreValideDirectement() {
        BonTransfert transfert = nouveauTransfertAvecLigne();
        assertThatThrownBy(() -> transfert.valider("super-admin@obv.ci"))
                .isInstanceOf(TransfertInvalideException.class);
    }

    @Test
    void annulerSansMotifEstRefuse() {
        BonTransfert transfert = nouveauTransfertAvecLigne();
        transfert.cloturer("gerant@obv.ci");
        assertThatThrownBy(() -> transfert.annuler(" ")).isInstanceOf(TransfertInvalideException.class);
    }

    @Test
    void annulerAvecMotifPasseAAnnulee() {
        BonTransfert transfert = nouveauTransfertAvecLigne();
        transfert.cloturer("gerant@obv.ci");
        transfert.annuler("Erreur de saisie");
        assertThat(transfert.getStatut()).isEqualTo(StatutTransfert.ANNULEE);
        assertThat(transfert.getMotifAnnulation()).isEqualTo("Erreur de saisie");
    }

    @Test
    void unTransfertValideNePeutPlusEtreModifie() {
        BonTransfert transfert = nouveauTransfertAvecLigne();
        transfert.cloturer("gerant@obv.ci");
        transfert.valider("super-admin@obv.ci");
        assertThatThrownBy(() -> transfert.ajouterLigne(produit, conditionnementPair, 1, new Montant(8000)))
                .isInstanceOf(TransfertInvalideException.class);
    }

    @Test
    void montantTotalSommeLesLignes() {
        BonTransfert transfert = new BonTransfert("BT-1-2026-000001", depot, bar, Instant.now());
        transfert.ajouterLigne(produit, conditionnementPair, 6, new Montant(8000));
        transfert.ajouterLigne(produit, conditionnementPair, 1, new Montant(8000));
        assertThat(transfert.montantTotal()).isEqualTo(new Montant(8000L * 6 / 2 + 8000L * 1 / 2));
    }
}
