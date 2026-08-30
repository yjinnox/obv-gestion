package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.reception.Reception;
import com.obvgestion.domain.reception.StatutReception;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReceptionJpaRepository extends JpaRepository<Reception, Long> {

    @Query("""
            SELECT r FROM Reception r
            JOIN FETCH r.fournisseur
            JOIN FETCH r.pointDeVente
            LEFT JOIN FETCH r.lignes l
            LEFT JOIN FETCH l.produit p
            LEFT JOIN FETCH p.marque
            LEFT JOIN FETCH p.volume
            LEFT JOIN FETCH l.conditionnement
            WHERE r.id = :id
            """)
    Optional<Reception> parIdAvecLignes(@Param("id") Long id);

    /**
     * Ne sélectionne que les identifiants : la pagination SQL n'est fiable
     * qu'en l'absence de fetch join sur une collection ({@code lignes}). Les
     * entités complètes sont récupérées séparément par {@link #parIdsAvecLignes}.
     */
    @Query(value = """
            SELECT r.id FROM Reception r
            WHERE (:pointDeVenteId IS NULL OR r.pointDeVente.id = :pointDeVenteId)
              AND (:statut IS NULL OR r.statut = :statut)
            ORDER BY r.dateHeureLivraison DESC
            """,
            countQuery = """
            SELECT COUNT(r) FROM Reception r
            WHERE (:pointDeVenteId IS NULL OR r.pointDeVente.id = :pointDeVenteId)
              AND (:statut IS NULL OR r.statut = :statut)
            """)
    Page<Long> rechercherIds(@Param("pointDeVenteId") Long pointDeVenteId,
                              @Param("statut") StatutReception statut,
                              Pageable pageable);

    @Query("""
            SELECT DISTINCT r FROM Reception r
            JOIN FETCH r.fournisseur
            JOIN FETCH r.pointDeVente
            LEFT JOIN FETCH r.lignes l
            LEFT JOIN FETCH l.produit p
            LEFT JOIN FETCH p.marque
            LEFT JOIN FETCH p.volume
            LEFT JOIN FETCH l.conditionnement
            WHERE r.id IN :ids
            """)
    List<Reception> parIdsAvecLignes(@Param("ids") List<Long> ids);
}
