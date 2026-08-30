package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.referentiel.Volume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VolumeJpaRepository extends JpaRepository<Volume, Long> {

    @Query("SELECT v FROM Volume v WHERE (:actif IS NULL OR v.actif = :actif)")
    Page<Volume> rechercher(@Param("actif") Boolean actif, Pageable pageable);
}
