package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.vente.SessionVente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SessionVenteJpaRepository extends JpaRepository<SessionVente, Long> {

    @Query("SELECT s FROM SessionVente s JOIN FETCH s.pointDeVente WHERE s.id = :id")
    Optional<SessionVente> parIdAvecPointDeVente(@Param("id") Long id);

    @Query("""
            SELECT s FROM SessionVente s JOIN FETCH s.pointDeVente
            WHERE s.pointDeVente.id = :pointDeVenteId AND s.statut = 'OUVERTE'
            """)
    Optional<SessionVente> sessionOuverte(@Param("pointDeVenteId") Long pointDeVenteId);

    @Query(value = """
            SELECT s FROM SessionVente s JOIN FETCH s.pointDeVente
            WHERE (:pointDeVenteId IS NULL OR s.pointDeVente.id = :pointDeVenteId)
            ORDER BY s.dateOuverture DESC
            """,
            countQuery = """
            SELECT COUNT(s) FROM SessionVente s
            WHERE (:pointDeVenteId IS NULL OR s.pointDeVente.id = :pointDeVenteId)
            """)
    Page<SessionVente> rechercher(@Param("pointDeVenteId") Long pointDeVenteId, Pageable pageable);
}
