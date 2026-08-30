package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.utilisateur.UtilisateurRepository;
import com.obvgestion.domain.utilisateur.RoleUtilisateur;
import com.obvgestion.domain.utilisateur.StatutUtilisateur;
import com.obvgestion.domain.utilisateur.Utilisateur;
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
class UtilisateurRepositoryAdapter implements UtilisateurRepository {

    private final UtilisateurJpaRepository jpaRepository;

    UtilisateurRepositoryAdapter(UtilisateurJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Utilisateur enregistrer(Utilisateur utilisateur) {
        return jpaRepository.save(utilisateur);
    }

    @Override
    public Optional<Utilisateur> parId(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Utilisateur> parEmail(String email) {
        return jpaRepository.findByEmail(email);
    }

    @Override
    public Optional<Utilisateur> parTelephone(String telephone) {
        return jpaRepository.findByTelephone(telephone);
    }

    @Override
    public Page<Utilisateur> rechercher(StatutUtilisateur statut, RoleUtilisateur role, Long pointDeVenteId,
                                         String recherche, Pageable pageable) {
        Page<Long> idsPage = jpaRepository.rechercherIds(statut, role, pointDeVenteId, recherche, pageable);
        List<Long> ids = idsPage.getContent();
        if (ids.isEmpty()) {
            return PageableExecutionUtils.getPage(List.of(), pageable, idsPage::getTotalElements);
        }

        Map<Long, Utilisateur> parId = jpaRepository.parIdsAvecAffectations(ids).stream()
                .collect(Collectors.toMap(Utilisateur::getId, Function.identity()));
        List<Utilisateur> contenu = ids.stream().map(parId::get).toList();

        return PageableExecutionUtils.getPage(contenu, pageable, idsPage::getTotalElements);
    }

    @Override
    public long compterParStatutEtRole(StatutUtilisateur statut, RoleUtilisateur role) {
        return jpaRepository.compterParStatutEtRole(statut, role);
    }

    @Override
    public boolean existeAuMoinsUnAvecRole(RoleUtilisateur role) {
        return jpaRepository.existeAuMoinsUnAvecRole(role);
    }
}
