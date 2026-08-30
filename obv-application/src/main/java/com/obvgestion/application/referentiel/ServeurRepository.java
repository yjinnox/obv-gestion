package com.obvgestion.application.referentiel;

import com.obvgestion.domain.referentiel.Serveur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ServeurRepository {

    Serveur enregistrer(Serveur serveur);

    Optional<Serveur> parId(Long id);

    Page<Serveur> rechercher(Long pointDeVenteId, Boolean actif, Pageable pageable);
}
