package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.audit.Journalisateur;
import com.obvgestion.domain.audit.JournalAction;
import com.obvgestion.domain.audit.TypeActionJournal;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
class JournalisateurAdapter implements Journalisateur {

    private final JournalActionRepository repository;

    JournalisateurAdapter(JournalActionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void journaliser(String acteur, TypeActionJournal action, String cibleType, String cibleId,
                             String valeursAvant, String valeursApres, String adresseIp) {
        repository.save(new JournalAction(
                acteur, action, cibleType, cibleId, valeursAvant, valeursApres, adresseIp, Instant.now()));
    }
}
