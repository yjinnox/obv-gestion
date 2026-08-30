package com.obvgestion.application.vente;

import com.obvgestion.domain.vente.Panier;

/** §8.2 — port du panier (Redis, TTL 4h), implémenté en infrastructure. */
public interface PanierRepository {

    /** Retourne un panier vide si aucun n'est encore persisté (ou expiré) pour cette clé. */
    Panier trouver(Long utilisateurId, Long sessionVenteId);

    void enregistrer(Panier panier);

    void supprimer(Long utilisateurId, Long sessionVenteId);
}
