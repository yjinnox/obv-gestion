package com.obvgestion.application.reception;

import com.obvgestion.domain.reception.Reception;
import com.obvgestion.domain.reception.StatutReception;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/** Port de persistance des réceptions, implémenté en infrastructure. */
public interface ReceptionRepository {

    Reception enregistrer(Reception reception);

    Optional<Reception> parId(Long id);

    Page<Reception> rechercher(Long pointDeVenteId, StatutReception statut, Pageable pageable);
}
