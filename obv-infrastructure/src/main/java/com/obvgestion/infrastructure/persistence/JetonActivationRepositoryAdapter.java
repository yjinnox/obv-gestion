package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.utilisateur.JetonActivationRepository;
import com.obvgestion.domain.utilisateur.JetonActivation;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class JetonActivationRepositoryAdapter implements JetonActivationRepository {

    private final JetonActivationJpaRepository jpaRepository;

    JetonActivationRepositoryAdapter(JetonActivationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public JetonActivation enregistrer(JetonActivation jeton) {
        return jpaRepository.save(jeton);
    }

    @Override
    public Optional<JetonActivation> parEmpreinte(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash);
    }
}
