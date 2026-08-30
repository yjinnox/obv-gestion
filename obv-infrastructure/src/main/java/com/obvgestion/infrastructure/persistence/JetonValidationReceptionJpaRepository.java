package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.reception.JetonValidationReception;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JetonValidationReceptionJpaRepository extends JpaRepository<JetonValidationReception, Long> {
}
