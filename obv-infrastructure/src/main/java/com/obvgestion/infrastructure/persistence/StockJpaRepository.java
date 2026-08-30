package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.stock.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StockJpaRepository extends JpaRepository<Stock, Long> {

    @Query("""
            SELECT s FROM Stock s
            JOIN FETCH s.pointDeVente
            JOIN FETCH s.produit p JOIN FETCH p.marque JOIN FETCH p.volume
            WHERE s.pointDeVente.id = :pointDeVenteId AND s.produit.id = :produitId
            """)
    Optional<Stock> parPointDeVenteEtProduit(@Param("pointDeVenteId") Long pointDeVenteId,
                                              @Param("produitId") Long produitId);

    @Query(value = """
            SELECT s FROM Stock s
            JOIN FETCH s.pointDeVente
            JOIN FETCH s.produit p JOIN FETCH p.marque JOIN FETCH p.volume
            WHERE (:pointDeVenteId IS NULL OR s.pointDeVente.id = :pointDeVenteId)
            """,
            countQuery = """
            SELECT COUNT(s) FROM Stock s
            WHERE (:pointDeVenteId IS NULL OR s.pointDeVente.id = :pointDeVenteId)
            """)
    Page<Stock> rechercher(@Param("pointDeVenteId") Long pointDeVenteId, Pageable pageable);
}
