package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.vente.VenteRepository;
import com.obvgestion.domain.vente.Vente;
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
class VenteRepositoryAdapter implements VenteRepository {

    private final VenteJpaRepository jpaRepository;

    VenteRepositoryAdapter(VenteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /** RG-27 — flush immédiat : une violation de l'index unique d'idempotence doit être visible ici, avant tout mouvement de stock. */
    @Override
    public Vente enregistrerEtValider(Vente vente) {
        return jpaRepository.saveAndFlush(vente);
    }

    @Override
    public Optional<Vente> parId(Long id) {
        return jpaRepository.parIdAvecLignes(id);
    }

    @Override
    public Optional<Vente> parIdempotencyKey(Long sessionVenteId, String idempotencyKey) {
        return jpaRepository.parIdempotencyKey(sessionVenteId, idempotencyKey);
    }

    @Override
    public List<Vente> parSession(Long sessionVenteId) {
        return jpaRepository.parSession(sessionVenteId);
    }

    @Override
    public Page<Vente> rechercher(Long sessionVenteId, Pageable pageable) {
        Page<Long> idsPage = jpaRepository.rechercherIds(sessionVenteId, pageable);
        List<Long> ids = idsPage.getContent();
        if (ids.isEmpty()) {
            return PageableExecutionUtils.getPage(List.of(), pageable, idsPage::getTotalElements);
        }

        Map<Long, Vente> parId = jpaRepository.parIdsAvecLignes(ids).stream()
                .collect(Collectors.toMap(Vente::getId, Function.identity()));
        List<Vente> contenu = ids.stream().map(parId::get).toList();

        return PageableExecutionUtils.getPage(contenu, pageable, idsPage::getTotalElements);
    }
}
