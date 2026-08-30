package com.obvgestion.application.referentiel;

import com.obvgestion.domain.referentiel.Conditionnement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ConditionnementRepository {

    Conditionnement enregistrer(Conditionnement conditionnement);

    Optional<Conditionnement> parId(Long id);

    Page<Conditionnement> rechercher(Long produitId, Boolean actif, Pageable pageable);
}
