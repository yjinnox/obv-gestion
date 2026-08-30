package com.obvgestion.api.vente;

import com.obvgestion.domain.referentiel.Client;
import com.obvgestion.domain.referentiel.TypeClient;
import com.obvgestion.domain.vente.Vente;

import java.time.Instant;
import java.util.List;

public record VenteResponse(Long id, Long sessionVenteId, Long clientId, String clientNom,
                             String numeroBonCommande, String numeroFacture, String modePaiement,
                             long montantSousTotalXof, long montantConsigneXof, long montantTvaXof,
                             long montantTotalXof, Instant dateHeure, List<LigneVenteResponse> lignes) {

    public VenteResponse {
        lignes = List.copyOf(lignes);
    }

    public static VenteResponse de(Vente vente) {
        return new VenteResponse(
                vente.getId(), vente.getSessionVente().getId(), vente.getClient().getId(), nomClient(vente.getClient()),
                vente.getNumeroBonCommande(), vente.getNumeroFacture(), vente.getModePaiement().name(),
                vente.getMontantSousTotal().valeurXof(), vente.getMontantConsigne().valeurXof(),
                vente.getMontantTva().valeurXof(), vente.getMontantTotal().valeurXof(), vente.getDateHeure(),
                vente.getLignes().stream().map(LigneVenteResponse::de).toList());
    }

    private static String nomClient(Client client) {
        return client.getType() == TypeClient.ENTREPRISE
                ? client.getRaisonSociale() : client.getNom() + " " + client.getPrenoms();
    }
}
