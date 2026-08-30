package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.vente.Vente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface VenteJpaRepository extends JpaRepository<Vente, Long> {

    @Query("""
            SELECT v FROM Vente v
            JOIN FETCH v.client
            JOIN FETCH v.sessionVente sv JOIN FETCH sv.pointDeVente
            LEFT JOIN FETCH v.lignes l
            LEFT JOIN FETCH l.produit p
            LEFT JOIN FETCH p.marque
            LEFT JOIN FETCH p.volume
            WHERE v.id = :id
            """)
    Optional<Vente> parIdAvecLignes(@Param("id") Long id);

    @Query("""
            SELECT v FROM Vente v
            JOIN FETCH v.client
            JOIN FETCH v.sessionVente sv JOIN FETCH sv.pointDeVente
            LEFT JOIN FETCH v.lignes l
            LEFT JOIN FETCH l.produit p
            LEFT JOIN FETCH p.marque
            LEFT JOIN FETCH p.volume
            WHERE v.sessionVente.id = :sessionVenteId AND v.idempotencyKey = :idempotencyKey
            """)
    Optional<Vente> parIdempotencyKey(@Param("sessionVenteId") Long sessionVenteId,
                                       @Param("idempotencyKey") String idempotencyKey);

    @Query("""
            SELECT v FROM Vente v
            JOIN FETCH v.client
            LEFT JOIN FETCH v.lignes l
            LEFT JOIN FETCH l.produit p
            LEFT JOIN FETCH p.marque
            LEFT JOIN FETCH p.volume
            WHERE v.sessionVente.id = :sessionVenteId
            """)
    List<Vente> parSession(@Param("sessionVenteId") Long sessionVenteId);

    /**
     * §13 — ventes d'un point de vente sur une période, pour le rapport de
     * ventes. {@code du}/{@code au} sont toujours renseignés par l'appelant
     * (bornes larges par défaut si absentes côté API, comme pour
     * {@link MouvementStockJpaRepository#rechercher}) : un paramètre à la
     * fois {@code IS NULL}-testé et comparé à une colonne {@code timestamptz}
     * empêche PostgreSQL d'en inférer le type côté protocole étendu.
     */
    @Query("""
            SELECT v FROM Vente v
            JOIN FETCH v.client
            LEFT JOIN FETCH v.lignes l
            LEFT JOIN FETCH l.produit p
            LEFT JOIN FETCH p.marque
            LEFT JOIN FETCH p.volume
            WHERE v.sessionVente.pointDeVente.id = :pointDeVenteId
              AND v.dateHeure >= :du
              AND v.dateHeure <= :au
            """)
    List<Vente> parPointDeVenteEtPeriode(@Param("pointDeVenteId") Long pointDeVenteId, @Param("du") Instant du,
                                          @Param("au") Instant au);

    /**
     * Ne sélectionne que les identifiants : la pagination SQL n'est fiable
     * qu'en l'absence de fetch join sur une collection ({@code lignes}). Les
     * entités complètes sont récupérées séparément par {@link #parIdsAvecLignes}.
     */
    @Query(value = "SELECT v.id FROM Vente v WHERE (:sessionVenteId IS NULL OR v.sessionVente.id = :sessionVenteId) " +
            "ORDER BY v.dateHeure DESC",
            countQuery = "SELECT COUNT(v) FROM Vente v WHERE (:sessionVenteId IS NULL OR v.sessionVente.id = :sessionVenteId)")
    Page<Long> rechercherIds(@Param("sessionVenteId") Long sessionVenteId, Pageable pageable);

    @Query("""
            SELECT DISTINCT v FROM Vente v
            JOIN FETCH v.client
            LEFT JOIN FETCH v.lignes l
            LEFT JOIN FETCH l.produit p
            LEFT JOIN FETCH p.marque
            LEFT JOIN FETCH p.volume
            WHERE v.id IN :ids
            """)
    List<Vente> parIdsAvecLignes(@Param("ids") List<Long> ids);
}
