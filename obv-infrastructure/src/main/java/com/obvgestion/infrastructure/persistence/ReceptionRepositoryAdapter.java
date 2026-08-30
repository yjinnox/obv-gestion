package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.reception.ReceptionRepository;
import com.obvgestion.domain.reception.Reception;
import com.obvgestion.domain.reception.StatutReception;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
class ReceptionRepositoryAdapter implements ReceptionRepository {

    private final ReceptionJpaRepository jpaRepository;

    ReceptionRepositoryAdapter(ReceptionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Reception enregistrer(Reception reception) {
        return jpaRepository.save(reception);
    }

    @Override
    public Optional<Reception> parId(Long id) {
        return jpaRepository.parIdAvecLignes(id);
    }

    @Override
    public Page<Reception> rechercher(Long pointDeVenteId, StatutReception statut, Pageable pageable) {
        Page<Long> idsPage = jpaRepository.rechercherIds(pointDeVenteId, statut, pageable);
        List<Long> ids = idsPage.getContent();
        if (ids.isEmpty()) {
            return PageableExecutionUtils.getPage(List.of(), pageable, idsPage::getTotalElements);
        }

        Map<Long, Reception> parId = jpaRepository.parIdsAvecLignes(ids).stream()
                .collect(Collectors.toMap(Reception::getId, Function.identity()));
        List<Reception> contenu = ids.stream().map(parId::get).toList();

        return PageableExecutionUtils.getPage(contenu, pageable, idsPage::getTotalElements);
    }
}
