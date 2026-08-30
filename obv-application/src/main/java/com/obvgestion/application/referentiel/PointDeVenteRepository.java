package com.obvgestion.application.referentiel;

import com.obvgestion.domain.referentiel.PointDeVente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/** Port de persistance du référentiel des points de vente (§5.1). */
public interface PointDeVenteRepository {

    PointDeVente enregistrer(PointDeVente pointDeVente);

    Optional<PointDeVente> parId(Long id);

    Page<PointDeVente> rechercher(Boolean actif, Pageable pageable);
}
