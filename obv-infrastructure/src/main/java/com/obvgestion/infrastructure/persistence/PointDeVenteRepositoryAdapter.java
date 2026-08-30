package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.referentiel.PointDeVenteRepository;
import com.obvgestion.domain.referentiel.PointDeVente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class PointDeVenteRepositoryAdapter implements PointDeVenteRepository {

    private final PointDeVenteJpaRepository jpaRepository;

    PointDeVenteRepositoryAdapter(PointDeVenteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PointDeVente enregistrer(PointDeVente pointDeVente) {
        return jpaRepository.save(pointDeVente);
    }

    @Override
    public Optional<PointDeVente> parId(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Page<PointDeVente> rechercher(Boolean actif, Pageable pageable) {
        return jpaRepository.rechercher(actif, pageable);
    }
}
