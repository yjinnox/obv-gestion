package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.referentiel.ProduitRepository;
import com.obvgestion.domain.referentiel.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class ProduitRepositoryAdapter implements ProduitRepository {

    private final ProduitJpaRepository jpaRepository;

    ProduitRepositoryAdapter(ProduitJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Produit enregistrer(Produit produit) {
        return jpaRepository.save(produit);
    }

    @Override
    public Optional<Produit> parId(Long id) {
        return jpaRepository.parIdAvecMarqueEtVolume(id);
    }

    @Override
    public Page<Produit> rechercher(Long marqueId, Long volumeId, Boolean actif, Pageable pageable) {
        return jpaRepository.rechercher(marqueId, volumeId, actif, pageable);
    }
}
