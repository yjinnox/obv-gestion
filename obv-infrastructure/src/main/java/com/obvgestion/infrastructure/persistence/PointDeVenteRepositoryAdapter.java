package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.referentiel.PointDeVenteRepository;
import com.obvgestion.domain.referentiel.PointDeVente;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class PointDeVenteRepositoryAdapter implements PointDeVenteRepository {

    private final PointDeVenteJpaRepository jpaRepository;

    PointDeVenteRepositoryAdapter(PointDeVenteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<PointDeVente> parId(Long id) {
        return jpaRepository.findById(id);
    }
}
