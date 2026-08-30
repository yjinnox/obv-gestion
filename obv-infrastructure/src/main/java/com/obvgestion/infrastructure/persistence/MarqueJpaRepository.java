package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.referentiel.Marque;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MarqueJpaRepository extends JpaRepository<Marque, Long> {

    @Query("SELECT m FROM Marque m WHERE (:actif IS NULL OR m.actif = :actif)")
    Page<Marque> rechercher(@Param("actif") Boolean actif, Pageable pageable);
}
