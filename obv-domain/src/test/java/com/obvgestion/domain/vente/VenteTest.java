package com.obvgestion.domain.vente;

import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.referentiel.Client;
import com.obvgestion.domain.referentiel.Marque;
import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.referentiel.Produit;
import com.obvgestion.domain.referentiel.TypeClient;
import com.obvgestion.domain.referentiel.TypePointDeVente;
import com.obvgestion.domain.referentiel.Volume;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VenteTest {

    private final PointDeVente depot = new PointDeVente("Dépôt central", TypePointDeVente.DEPOT, "Abidjan");
    private final Produit produit = new Produit(new Marque("Flag"), new Volume("33 cl", 330));
    private final Client client = Client.creer(TypeClient.PARTICULIER, "Yao", "Marie", null, "0102030405", null, null);

    private SessionVente sessionOuverte() {
        return new SessionVente(depot, "1", new Montant(20000), Instant.now());
    }

    private List<LigneVenteDemandee> uneLigne(long quantiteDemiCasiers) {
        return List.of(new LigneVenteDemandee(produit, quantiteDemiCasiers, new Montant(9500), new Montant(500)));
    }

    @Test
    void commanderAvecPanierVideEstRefuse() {
        assertThatThrownBy(() -> Vente.commander(sessionOuverte(), client, "BC-1", "FA-1", ModePaiement.ESPECES,
                List.of(), 18, "clef-1", Instant.now())).isInstanceOf(VenteInvalideException.class);
    }

    @Test
    void commanderHorsSessionOuverteEstRefuse() {
        SessionVente session = sessionOuverte();
        session.cloturer("1", Montant.zero(), Montant.zero(), Instant.now());
        assertThatThrownBy(() -> Vente.commander(session, client, "BC-1", "FA-1", ModePaiement.ESPECES,
                uneLigne(4), 18, "clef-1", Instant.now())).isInstanceOf(SessionVenteInvalideException.class);
    }

    /** §20.5 — TVA 18% appliquée au seul sous-total, jamais à la consigne. RG-11 : 6 demi-casiers = 3 casiers. */
    @Test
    void commanderCalculeLesMontantsAvecTvaSurLeSousTotalUniquement() {
        Vente vente = Vente.commander(sessionOuverte(), client, "BC-1-2026-000001", "FA-1-2026-000001",
                ModePaiement.ESPECES, uneLigne(6), 18, "clef-1", Instant.now());

        assertThat(vente.getMontantSousTotal()).isEqualTo(new Montant(3 * 9500));
        assertThat(vente.getMontantConsigne()).isEqualTo(new Montant(3 * 500));
        assertThat(vente.getMontantTva()).isEqualTo(new Montant(Math.round(3 * 9500 * 0.18)));
        assertThat(vente.getMontantTotal()).isEqualTo(
                vente.getMontantSousTotal().plus(vente.getMontantTva()).plus(vente.getMontantConsigne()));
    }

    /** RG-11 — un demi-casier (quantité impaire) facture la moitié du prix du casier. */
    @Test
    void unDemiCasierFactureLaMoitieDuPrixDuCasier() {
        Vente vente = Vente.commander(sessionOuverte(), client, "BC-1", "FA-1", ModePaiement.ESPECES,
                uneLigne(1), 18, "clef-1", Instant.now());
        assertThat(vente.getMontantSousTotal()).isEqualTo(new Montant(9500 / 2));
    }

    /** RG-29 — correction des quantités réservée à une session en modification. */
    @Test
    void modifierQuantiteLigneHorsModificationEstRefuse() {
        Vente vente = Vente.commander(sessionOuverte(), client, "BC-1", "FA-1", ModePaiement.ESPECES,
                uneLigne(4), 18, "clef-1", Instant.now());
        Long ligneId = vente.getLignes().get(0).getId();
        assertThatThrownBy(() -> vente.modifierQuantiteLigne(ligneId, 5, 18))
                .isInstanceOf(SessionVenteInvalideException.class);
    }

    // La correction réussie d'une ligne (modifierQuantiteLigne en EN_MODIFICATION) nécessite un identifiant de
    // ligne réellement attribué par JPA : elle est validée en conditions réelles (P4), comme RG-20 l'a été en P3.
}
