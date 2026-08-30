package com.obvgestion.application.vente;

import com.obvgestion.domain.vente.SessionVente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/** Port de persistance des sessions de vente, implémenté en infrastructure. */
public interface SessionVenteRepository {

    SessionVente enregistrer(SessionVente session);

    Optional<SessionVente> parId(Long id);

    /** RG-23 — la session actuellement ouverte pour ce point de vente, s'il y en a une. */
    Optional<SessionVente> sessionOuverte(Long pointDeVenteId);

    Page<SessionVente> rechercher(Long pointDeVenteId, Pageable pageable);
}
