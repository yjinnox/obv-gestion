package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.referentiel.ServeurRepository;
import com.obvgestion.domain.referentiel.Serveur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class ServeurRepositoryAdapter implements ServeurRepository {

    private final ServeurJpaRepository jpaRepository;

    ServeurRepositoryAdapter(ServeurJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Serveur enregistrer(Serveur serveur) {
        return jpaRepository.save(serveur);
    }

    @Override
    public Optional<Serveur> parId(Long id) {
        return jpaRepository.parIdAvecPointDeVente(id);
    }

    @Override
    public Page<Serveur> rechercher(Long pointDeVenteId, Boolean actif, Pageable pageable) {
        return jpaRepository.rechercher(pointDeVenteId, actif, pageable);
    }
}
