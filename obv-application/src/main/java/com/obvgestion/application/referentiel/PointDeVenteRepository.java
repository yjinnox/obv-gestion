package com.obvgestion.application.referentiel;

import com.obvgestion.domain.referentiel.PointDeVente;

import java.util.Optional;

/** Port de lecture du référentiel des points de vente (CRUD complet en P2, §5.1). */
public interface PointDeVenteRepository {

    Optional<PointDeVente> parId(Long id);
}
