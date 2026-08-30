package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.referentiel.FournisseurRepository;
import com.obvgestion.domain.referentiel.Fournisseur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class FournisseurRepositoryAdapter implements FournisseurRepository {

    private final FournisseurJpaRepository jpaRepository;

    FournisseurRepositoryAdapter(FournisseurJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Fournisseur enregistrer(Fournisseur fournisseur) {
        return jpaRepository.save(fournisseur);
    }

    @Override
    public Optional<Fournisseur> parId(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Page<Fournisseur> rechercher(Boolean actif, Pageable pageable) {
        return jpaRepository.rechercher(actif, pageable);
    }
}
