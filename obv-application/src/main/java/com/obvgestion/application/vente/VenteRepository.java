package com.obvgestion.application.vente;

import com.obvgestion.domain.vente.Vente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Port de persistance des ventes, implémenté en infrastructure. */
public interface VenteRepository {

    /**
     * RG-27 — enregistre et force le flush : une violation de l'index unique
     * {@code (sessionVente, idempotencyKey)} (double commande) est levée
     * immédiatement, dans la transaction de l'appelant, avant toute
     * décrémentation de stock.
     */
    Vente enregistrerEtValider(Vente vente);

    Optional<Vente> parId(Long id);

    Optional<Vente> parIdempotencyKey(Long sessionVenteId, String idempotencyKey);

    /** Ventes d'une session, pour le calcul du total théorique à la clôture (§8.3). */
    List<Vente> parSession(Long sessionVenteId);

    /** §13 — ventes d'un point de vente sur une période, pour le rapport de ventes. Bornes nullables (illimitées). */
    List<Vente> parPointDeVenteEtPeriode(Long pointDeVenteId, Instant du, Instant au);

    Page<Vente> rechercher(Long sessionVenteId, Pageable pageable);
}
