package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.utilisateur.JetonActivation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JetonActivationJpaRepository extends JpaRepository<JetonActivation, Long> {

    Optional<JetonActivation> findByTokenHash(String tokenHash);
}
