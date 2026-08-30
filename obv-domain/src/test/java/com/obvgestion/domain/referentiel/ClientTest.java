package com.obvgestion.domain.referentiel;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** RG-07 — champs obligatoires/interdits selon le type de client. */
class ClientTest {

    @Test
    void entrepriseExigeUneRaisonSociale() {
        assertThatThrownBy(() -> Client.creer(TypeClient.ENTREPRISE, null, null, null, "+2250700000000", null, null))
                .isInstanceOf(ClientInvalideException.class);
    }

    @Test
    void entrepriseAvecRaisonSocialeEstAcceptee() {
        Client client = Client.creer(
                TypeClient.ENTREPRISE, null, null, "SARL Boissons", "+2250700000000", null, null);
        assertThat(client.getRaisonSociale()).isEqualTo("SARL Boissons");
    }

    @Test
    void particulierInterditRaisonSociale() {
        assertThatThrownBy(() -> Client.creer(
                TypeClient.PARTICULIER, "Kouassi", "Awa", "SARL Boissons", "+2250700000000", null, null))
                .isInstanceOf(ClientInvalideException.class);
    }

    @Test
    void particulierExigeNomEtPrenoms() {
        assertThatThrownBy(() -> Client.creer(
                TypeClient.PARTICULIER, null, null, null, "+2250700000000", null, null))
                .isInstanceOf(ClientInvalideException.class);
    }

    @Test
    void particulierAvecNomEtPrenomsEstAccepte() {
        Client client = Client.creer(
                TypeClient.PARTICULIER, "Kouassi", "Awa", null, "+2250700000000", null, null);
        assertThat(client.getNom()).isEqualTo("Kouassi");
        assertThat(client.getPrenoms()).isEqualTo("Awa");
    }

    @Test
    void telephoneObligatoireQuelQueSoitLeType() {
        assertThatThrownBy(() -> Client.creer(TypeClient.PARTICULIER, "Kouassi", "Awa", null, null, null, null))
                .isInstanceOf(ClientInvalideException.class);
    }
}
