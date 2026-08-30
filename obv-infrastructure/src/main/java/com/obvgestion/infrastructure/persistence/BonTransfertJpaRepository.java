package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.transfert.BonTransfert;
import com.obvgestion.domain.transfert.StatutTransfert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BonTransfertJpaRepository extends JpaRepository<BonTransfert, Long> {

    @Query("""
            SELECT t FROM BonTransfert t
            JOIN FETCH t.pointDeVenteSource
            JOIN FETCH t.pointDeVenteDestination
            LEFT JOIN FETCH t.lignes l
            LEFT JOIN FETCH l.produit p
            LEFT JOIN FETCH p.marque
            LEFT JOIN FETCH p.volume
            LEFT JOIN FETCH l.conditionnement
            WHERE t.id = :id
            """)
    Optional<BonTransfert> parIdAvecLignes(@Param("id") Long id);

    /**
     * Ne sélectionne que les identifiants : la pagination SQL n'est fiable
     * qu'en l'absence de fetch join sur une collection ({@code lignes}). Les
     * entités complètes sont récupérées séparément par {@link #parIdsAvecLignes}.
     */
    @Query(value = """
            SELECT t.id FROM BonTransfert t
            WHERE (:pointDeVenteSourceId IS NULL OR t.pointDeVenteSource.id = :pointDeVenteSourceId)
              AND (:pointDeVenteDestinationId IS NULL OR t.pointDeVenteDestination.id = :pointDeVenteDestinationId)
              AND (:statut IS NULL OR t.statut = :statut)
            ORDER BY t.dateHeure DESC
            """,
            countQuery = """
            SELECT COUNT(t) FROM BonTransfert t
            WHERE (:pointDeVenteSourceId IS NULL OR t.pointDeVenteSource.id = :pointDeVenteSourceId)
              AND (:pointDeVenteDestinationId IS NULL OR t.pointDeVenteDestination.id = :pointDeVenteDestinationId)
              AND (:statut IS NULL OR t.statut = :statut)
            """)
    Page<Long> rechercherIds(@Param("pointDeVenteSourceId") Long pointDeVenteSourceId,
                              @Param("pointDeVenteDestinationId") Long pointDeVenteDestinationId,
                              @Param("statut") StatutTransfert statut,
                              Pageable pageable);

    @Query("""
            SELECT DISTINCT t FROM BonTransfert t
            JOIN FETCH t.pointDeVenteSource
            JOIN FETCH t.pointDeVenteDestination
            LEFT JOIN FETCH t.lignes l
            LEFT JOIN FETCH l.produit p
            LEFT JOIN FETCH p.marque
            LEFT JOIN FETCH p.volume
            LEFT JOIN FETCH l.conditionnement
            WHERE t.id IN :ids
            """)
    List<BonTransfert> parIdsAvecLignes(@Param("ids") List<Long> ids);
}
