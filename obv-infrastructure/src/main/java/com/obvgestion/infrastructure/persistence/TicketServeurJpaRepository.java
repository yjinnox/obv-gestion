package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.bar.StatutTicketServeur;
import com.obvgestion.domain.bar.TicketServeur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TicketServeurJpaRepository extends JpaRepository<TicketServeur, Long> {

    @Query("""
            SELECT t FROM TicketServeur t
            JOIN FETCH t.serveur
            JOIN FETCH t.sessionVente sv JOIN FETCH sv.pointDeVente
            LEFT JOIN FETCH t.lignes l
            LEFT JOIN FETCH l.produit p
            LEFT JOIN FETCH p.marque
            LEFT JOIN FETCH p.volume
            WHERE t.id = :id
            """)
    Optional<TicketServeur> parIdAvecLignes(@Param("id") Long id);

    @Query("""
            SELECT t FROM TicketServeur t
            JOIN FETCH t.serveur
            LEFT JOIN FETCH t.lignes l
            LEFT JOIN FETCH l.produit p
            LEFT JOIN FETCH p.marque
            LEFT JOIN FETCH p.volume
            WHERE t.sessionVente.id = :sessionVenteId
            """)
    List<TicketServeur> parSession(@Param("sessionVenteId") Long sessionVenteId);

    /**
     * §13 — tickets encaissés d'un point de vente sur une période, pour le
     * rapport de ventes. {@code du}/{@code au} sont toujours renseignés par
     * l'appelant (bornes larges par défaut si absentes côté API, comme pour
     * {@link MouvementStockJpaRepository#rechercher}) : un paramètre à la
     * fois {@code IS NULL}-testé et comparé à une colonne {@code timestamptz}
     * empêche PostgreSQL d'en inférer le type côté protocole étendu.
     */
    @Query("""
            SELECT t FROM TicketServeur t
            JOIN FETCH t.serveur
            LEFT JOIN FETCH t.lignes l
            LEFT JOIN FETCH l.produit p
            LEFT JOIN FETCH p.marque
            LEFT JOIN FETCH p.volume
            WHERE t.sessionVente.pointDeVente.id = :pointDeVenteId
              AND t.statut = :statut
              AND t.dateEncaissement >= :du
              AND t.dateEncaissement <= :au
            """)
    List<TicketServeur> parPointDeVenteEtPeriode(@Param("pointDeVenteId") Long pointDeVenteId, @Param("du") Instant du,
                                                  @Param("au") Instant au, @Param("statut") StatutTicketServeur statut);

    /**
     * Ne sélectionne que les identifiants : la pagination SQL n'est fiable
     * qu'en l'absence de fetch join sur une collection ({@code lignes}). Les
     * entités complètes sont récupérées séparément par {@link #parIdsAvecLignes}.
     */
    @Query(value = """
            SELECT t.id FROM TicketServeur t
            WHERE (:sessionVenteId IS NULL OR t.sessionVente.id = :sessionVenteId)
              AND (:serveurId IS NULL OR t.serveur.id = :serveurId)
              AND (:statut IS NULL OR t.statut = :statut)
            ORDER BY t.id DESC
            """,
            countQuery = """
            SELECT COUNT(t) FROM TicketServeur t
            WHERE (:sessionVenteId IS NULL OR t.sessionVente.id = :sessionVenteId)
              AND (:serveurId IS NULL OR t.serveur.id = :serveurId)
              AND (:statut IS NULL OR t.statut = :statut)
            """)
    Page<Long> rechercherIds(@Param("sessionVenteId") Long sessionVenteId, @Param("serveurId") Long serveurId,
                              @Param("statut") StatutTicketServeur statut, Pageable pageable);

    @Query("""
            SELECT DISTINCT t FROM TicketServeur t
            JOIN FETCH t.serveur
            LEFT JOIN FETCH t.lignes l
            LEFT JOIN FETCH l.produit p
            LEFT JOIN FETCH p.marque
            LEFT JOIN FETCH p.volume
            WHERE t.id IN :ids
            """)
    List<TicketServeur> parIdsAvecLignes(@Param("ids") List<Long> ids);
}
