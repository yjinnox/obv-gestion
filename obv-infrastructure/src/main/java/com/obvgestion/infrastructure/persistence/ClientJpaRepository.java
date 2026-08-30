package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.referentiel.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientJpaRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByTelephone(String telephone);

    @Query("""
            SELECT c FROM Client c
            WHERE (:actif IS NULL OR c.actif = :actif)
              AND (:recherche IS NULL
                   OR LOWER(c.nom) LIKE LOWER(CONCAT('%', CAST(:recherche AS string), '%'))
                   OR LOWER(c.prenoms) LIKE LOWER(CONCAT('%', CAST(:recherche AS string), '%'))
                   OR LOWER(c.raisonSociale) LIKE LOWER(CONCAT('%', CAST(:recherche AS string), '%')))
            """)
    Page<Client> rechercher(@Param("actif") Boolean actif, @Param("recherche") String recherche, Pageable pageable);
}
