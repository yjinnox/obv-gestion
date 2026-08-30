package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.referentiel.ConditionnementRepository;
import com.obvgestion.domain.referentiel.Conditionnement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class ConditionnementRepositoryAdapter implements ConditionnementRepository {

    private final ConditionnementJpaRepository jpaRepository;

    ConditionnementRepositoryAdapter(ConditionnementJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Conditionnement enregistrer(Conditionnement conditionnement) {
        return jpaRepository.save(conditionnement);
    }

    @Override
    public Optional<Conditionnement> parId(Long id) {
        return jpaRepository.parIdAvecProduit(id);
    }

    @Override
    public Page<Conditionnement> rechercher(Long produitId, Boolean actif, Pageable pageable) {
        return jpaRepository.rechercher(produitId, actif, pageable);
    }
}
