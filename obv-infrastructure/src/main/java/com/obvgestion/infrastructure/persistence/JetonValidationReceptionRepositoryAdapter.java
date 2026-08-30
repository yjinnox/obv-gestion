package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.reception.JetonValidationReceptionRepository;
import com.obvgestion.domain.reception.JetonValidationReception;
import org.springframework.stereotype.Repository;

@Repository
class JetonValidationReceptionRepositoryAdapter implements JetonValidationReceptionRepository {

    private final JetonValidationReceptionJpaRepository jpaRepository;

    JetonValidationReceptionRepositoryAdapter(JetonValidationReceptionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public JetonValidationReception enregistrer(JetonValidationReception jeton) {
        return jpaRepository.save(jeton);
    }
}
