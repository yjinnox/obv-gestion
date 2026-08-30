package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.referentiel.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProduitJpaRepository extends JpaRepository<Produit, Long> {

    @Query("SELECT p FROM Produit p JOIN FETCH p.marque JOIN FETCH p.volume WHERE p.id = :id")
    Optional<Produit> parIdAvecMarqueEtVolume(@Param("id") Long id);

    @Query(value = """
            SELECT p FROM Produit p
            JOIN FETCH p.marque
            JOIN FETCH p.volume
            WHERE (:marqueId IS NULL OR p.marque.id = :marqueId)
              AND (:volumeId IS NULL OR p.volume.id = :volumeId)
              AND (:actif IS NULL OR p.actif = :actif)
            """,
            countQuery = """
            SELECT COUNT(p) FROM Produit p
            WHERE (:marqueId IS NULL OR p.marque.id = :marqueId)
              AND (:volumeId IS NULL OR p.volume.id = :volumeId)
              AND (:actif IS NULL OR p.actif = :actif)
            """)
    Page<Produit> rechercher(@Param("marqueId") Long marqueId, @Param("volumeId") Long volumeId,
                              @Param("actif") Boolean actif, Pageable pageable);
}
