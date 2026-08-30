package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.referentiel.MarqueRepository;
import com.obvgestion.domain.referentiel.Marque;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class MarqueRepositoryAdapter implements MarqueRepository {

    private final MarqueJpaRepository jpaRepository;

    MarqueRepositoryAdapter(MarqueJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Marque enregistrer(Marque marque) {
        return jpaRepository.save(marque);
    }

    @Override
    public Optional<Marque> parId(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Page<Marque> rechercher(Boolean actif, Pageable pageable) {
        return jpaRepository.rechercher(actif, pageable);
    }
}
