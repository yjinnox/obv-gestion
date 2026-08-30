package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.bar.TicketServeurRepository;
import com.obvgestion.domain.bar.StatutTicketServeur;
import com.obvgestion.domain.bar.TicketServeur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
class TicketServeurRepositoryAdapter implements TicketServeurRepository {

    /** Bornes larges substituées à {@code du}/{@code au} absents (même raison que MouvementStockRepositoryAdapter). */
    private static final Instant DEBUT_DEFAUT = Instant.parse("0001-01-01T00:00:00Z");
    private static final Instant FIN_DEFAUT = Instant.parse("9999-12-31T23:59:59Z");

    private final TicketServeurJpaRepository jpaRepository;

    TicketServeurRepositoryAdapter(TicketServeurJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public TicketServeur enregistrer(TicketServeur ticket) {
        return jpaRepository.save(ticket);
    }

    @Override
    public Optional<TicketServeur> parId(Long id) {
        return jpaRepository.parIdAvecLignes(id);
    }

    @Override
    public List<TicketServeur> parSession(Long sessionVenteId) {
        return jpaRepository.parSession(sessionVenteId);
    }

    @Override
    public List<TicketServeur> parPointDeVenteEtPeriode(Long pointDeVenteId, Instant du, Instant au) {
        return jpaRepository.parPointDeVenteEtPeriode(pointDeVenteId, du == null ? DEBUT_DEFAUT : du,
                au == null ? FIN_DEFAUT : au, StatutTicketServeur.ENCAISSE);
    }

    @Override
    public Page<TicketServeur> rechercher(Long sessionVenteId, Long serveurId, StatutTicketServeur statut,
                                           Pageable pageable) {
        Page<Long> idsPage = jpaRepository.rechercherIds(sessionVenteId, serveurId, statut, pageable);
        List<Long> ids = idsPage.getContent();
        if (ids.isEmpty()) {
            return PageableExecutionUtils.getPage(List.of(), pageable, idsPage::getTotalElements);
        }

        Map<Long, TicketServeur> parId = jpaRepository.parIdsAvecLignes(ids).stream()
                .collect(Collectors.toMap(TicketServeur::getId, Function.identity()));
        List<TicketServeur> contenu = ids.stream().map(parId::get).toList();

        return PageableExecutionUtils.getPage(contenu, pageable, idsPage::getTotalElements);
    }
}
