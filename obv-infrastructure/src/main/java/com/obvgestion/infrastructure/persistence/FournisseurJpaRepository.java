package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.referentiel.Fournisseur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FournisseurJpaRepository extends JpaRepository<Fournisseur, Long> {

    @Query("SELECT f FROM Fournisseur f WHERE (:actif IS NULL OR f.actif = :actif)")
    Page<Fournisseur> rechercher(@Param("actif") Boolean actif, Pageable pageable);
}
