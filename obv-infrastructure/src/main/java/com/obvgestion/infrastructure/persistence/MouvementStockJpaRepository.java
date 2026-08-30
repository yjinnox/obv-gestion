package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.stock.MouvementStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface MouvementStockJpaRepository extends JpaRepository<MouvementStock, Long> {

    /**
     * {@code du}/{@code au} sont toujours renseignés par l'appelant (bornes
     * larges par défaut si absentes côté API) : un paramètre à la fois
     * {@code IS NULL}-testé et comparé à une colonne {@code timestamptz}
     * empêche PostgreSQL d'en inférer le type côté protocole étendu.
     */
    @Query(value = """
            SELECT m FROM MouvementStock m
            JOIN FETCH m.pointDeVente
            JOIN FETCH m.produit p JOIN FETCH p.marque JOIN FETCH p.volume
            JOIN FETCH m.utilisateur
            WHERE (:pointDeVenteId IS NULL OR m.pointDeVente.id = :pointDeVenteId)
              AND (:produitId IS NULL OR m.produit.id = :produitId)
              AND m.dateHeure >= :du
              AND m.dateHeure <= :au
            ORDER BY m.dateHeure DESC
            """,
            countQuery = """
            SELECT COUNT(m) FROM MouvementStock m
            WHERE (:pointDeVenteId IS NULL OR m.pointDeVente.id = :pointDeVenteId)
              AND (:produitId IS NULL OR m.produit.id = :produitId)
              AND m.dateHeure >= :du
              AND m.dateHeure <= :au
            """)
    Page<MouvementStock> rechercher(@Param("pointDeVenteId") Long pointDeVenteId,
                                     @Param("produitId") Long produitId,
                                     @Param("du") Instant du,
                                     @Param("au") Instant au,
                                     Pageable pageable);
}
