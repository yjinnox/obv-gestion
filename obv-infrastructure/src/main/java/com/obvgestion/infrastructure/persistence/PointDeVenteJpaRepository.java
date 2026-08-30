package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.referentiel.PointDeVente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointDeVenteJpaRepository extends JpaRepository<PointDeVente, Long> {
}
