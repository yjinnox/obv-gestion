package com.obvgestion.application.referentiel;

import com.obvgestion.domain.referentiel.Marque;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MarqueRepository {

    Marque enregistrer(Marque marque);

    Optional<Marque> parId(Long id);

    Page<Marque> rechercher(Boolean actif, Pageable pageable);
}
