package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.referentiel.Conditionnement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConditionnementJpaRepository extends JpaRepository<Conditionnement, Long> {

    @Query("""
            SELECT c FROM Conditionnement c
            JOIN FETCH c.produit p JOIN FETCH p.marque JOIN FETCH p.volume
            WHERE c.id = :id
            """)
    Optional<Conditionnement> parIdAvecProduit(@Param("id") Long id);

    @Query(value = """
            SELECT c FROM Conditionnement c
            JOIN FETCH c.produit p JOIN FETCH p.marque JOIN FETCH p.volume
            WHERE (:produitId IS NULL OR c.produit.id = :produitId)
              AND (:actif IS NULL OR c.actif = :actif)
            """,
            countQuery = """
            SELECT COUNT(c) FROM Conditionnement c
            WHERE (:produitId IS NULL OR c.produit.id = :produitId)
              AND (:actif IS NULL OR c.actif = :actif)
            """)
    Page<Conditionnement> rechercher(@Param("produitId") Long produitId, @Param("actif") Boolean actif,
                                      Pageable pageable);
}
