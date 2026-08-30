package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.vente.SessionVenteRepository;
import com.obvgestion.domain.vente.SessionVente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class SessionVenteRepositoryAdapter implements SessionVenteRepository {

    private final SessionVenteJpaRepository jpaRepository;

    SessionVenteRepositoryAdapter(SessionVenteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SessionVente enregistrer(SessionVente session) {
        return jpaRepository.save(session);
    }

    @Override
    public Optional<SessionVente> parId(Long id) {
        return jpaRepository.parIdAvecPointDeVente(id);
    }

    @Override
    public Optional<SessionVente> sessionOuverte(Long pointDeVenteId) {
        return jpaRepository.sessionOuverte(pointDeVenteId);
    }

    @Override
    public Page<SessionVente> rechercher(Long pointDeVenteId, Pageable pageable) {
        return jpaRepository.rechercher(pointDeVenteId, pageable);
    }
}
