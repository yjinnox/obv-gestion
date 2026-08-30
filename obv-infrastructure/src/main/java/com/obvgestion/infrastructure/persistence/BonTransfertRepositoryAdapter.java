package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.transfert.BonTransfertRepository;
import com.obvgestion.domain.transfert.BonTransfert;
import com.obvgestion.domain.transfert.StatutTransfert;
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
class BonTransfertRepositoryAdapter implements BonTransfertRepository {

    private final BonTransfertJpaRepository jpaRepository;

    BonTransfertRepositoryAdapter(BonTransfertJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public BonTransfert enregistrer(BonTransfert transfert) {
        return jpaRepository.save(transfert);
    }

    @Override
    public Optional<BonTransfert> parId(Long id) {
        return jpaRepository.parIdAvecLignes(id);
    }

    @Override
    public Page<BonTransfert> rechercher(Long pointDeVenteSourceId, Long pointDeVenteDestinationId,
                                          StatutTransfert statut, Pageable pageable) {
        Page<Long> idsPage = jpaRepository.rechercherIds(pointDeVenteSourceId, pointDeVenteDestinationId, statut,
                pageable);
        List<Long> ids = idsPage.getContent();
        if (ids.isEmpty()) {
            return PageableExecutionUtils.getPage(List.of(), pageable, idsPage::getTotalElements);
        }

        Map<Long, BonTransfert> parId = jpaRepository.parIdsAvecLignes(ids).stream()
                .collect(Collectors.toMap(BonTransfert::getId, Function.identity()));
        List<BonTransfert> contenu = ids.stream().map(parId::get).toList();

        return PageableExecutionUtils.getPage(contenu, pageable, idsPage::getTotalElements);
    }
}
