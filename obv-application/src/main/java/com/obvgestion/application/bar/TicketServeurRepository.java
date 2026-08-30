package com.obvgestion.application.bar;

import com.obvgestion.domain.bar.StatutTicketServeur;
import com.obvgestion.domain.bar.TicketServeur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/** Port de persistance des tickets serveur, implémenté en infrastructure. */
public interface TicketServeurRepository {

    TicketServeur enregistrer(TicketServeur ticket);

    Optional<TicketServeur> parId(Long id);

    /** Tickets d'une session, pour le calcul du total théorique à la clôture (§8.3/RG-34) et le récapitulatif (RG-33). */
    List<TicketServeur> parSession(Long sessionVenteId);

    Page<TicketServeur> rechercher(Long sessionVenteId, Long serveurId, StatutTicketServeur statut, Pageable pageable);
}
