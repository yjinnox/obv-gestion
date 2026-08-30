package com.obvgestion.api.referentiel;

import com.obvgestion.domain.referentiel.Client;

public record ClientResponse(Long id, String type, String nom, String prenoms, String raisonSociale,
                              String telephone, String email, String adresseFacturation, boolean actif) {

    public static ClientResponse de(Client client) {
        return new ClientResponse(
                client.getId(), client.getType().name(), client.getNom(), client.getPrenoms(),
                client.getRaisonSociale(), client.getTelephone(), client.getEmail(),
                client.getAdresseFacturation(), client.isActif());
    }
}
