package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.referentiel.Serveur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ServeurJpaRepository extends JpaRepository<Serveur, Long> {

    @Query("SELECT s FROM Serveur s JOIN FETCH s.pointDeVente WHERE s.id = :id")
    Optional<Serveur> parIdAvecPointDeVente(@Param("id") Long id);

    @Query(value = """
            SELECT s FROM Serveur s JOIN FETCH s.pointDeVente
            WHERE (:pointDeVenteId IS NULL OR s.pointDeVente.id = :pointDeVenteId)
              AND (:actif IS NULL OR s.actif = :actif)
            """,
            countQuery = """
            SELECT COUNT(s) FROM Serveur s
            WHERE (:pointDeVenteId IS NULL OR s.pointDeVente.id = :pointDeVenteId)
              AND (:actif IS NULL OR s.actif = :actif)
            """)
    Page<Serveur> rechercher(@Param("pointDeVenteId") Long pointDeVenteId, @Param("actif") Boolean actif,
                              Pageable pageable);
}
