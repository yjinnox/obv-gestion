package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.audit.JournalAction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalActionRepository extends JpaRepository<JournalAction, Long> {
}
