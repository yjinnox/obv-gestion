package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.referentiel.NatureTarif;
import com.obvgestion.domain.referentiel.Tarif;
import com.obvgestion.domain.referentiel.UniteVente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TarifJpaRepository extends JpaRepository<Tarif, Long> {

    @Query("""
            SELECT t FROM Tarif t
            WHERE t.pointDeVente.id = :pointDeVenteId AND t.produit.id = :produitId
              AND t.uniteVente = :uniteVente AND t.nature = :nature AND t.dateFin IS NULL
            """)
    Optional<Tarif> tarifOuvert(@Param("pointDeVenteId") Long pointDeVenteId, @Param("produitId") Long produitId,
                                 @Param("uniteVente") UniteVente uniteVente, @Param("nature") NatureTarif nature);

    @Query(value = """
            SELECT t FROM Tarif t
            JOIN FETCH t.pointDeVente
            JOIN FETCH t.produit p JOIN FETCH p.marque JOIN FETCH p.volume
            WHERE (:pointDeVenteId IS NULL OR t.pointDeVente.id = :pointDeVenteId)
              AND (:produitId IS NULL OR t.produit.id = :produitId)
              AND (:nature IS NULL OR t.nature = :nature)
            ORDER BY t.dateDebut DESC
            """,
            countQuery = """
            SELECT COUNT(t) FROM Tarif t
            WHERE (:pointDeVenteId IS NULL OR t.pointDeVente.id = :pointDeVenteId)
              AND (:produitId IS NULL OR t.produit.id = :produitId)
              AND (:nature IS NULL OR t.nature = :nature)
            """)
    Page<Tarif> rechercher(@Param("pointDeVenteId") Long pointDeVenteId, @Param("produitId") Long produitId,
                            @Param("nature") NatureTarif nature, Pageable pageable);
}
