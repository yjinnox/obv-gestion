package com.obvgestion.application.referentiel;

import com.obvgestion.domain.referentiel.Conditionnement;
import com.obvgestion.domain.referentiel.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class ConditionnementService {

    private final ConditionnementRepository repository;
    private final ProduitRepository produitRepository;

    public ConditionnementService(ConditionnementRepository repository, ProduitRepository produitRepository) {
        this.repository = repository;
        this.produitRepository = produitRepository;
    }

    @Transactional
    public Conditionnement creer(Long produitId, int capaciteBouteilles) {
        Produit produit = produitRepository.parId(produitId)
                .orElseThrow(() -> new NoSuchElementException("Produit introuvable : " + produitId));
        return repository.enregistrer(new Conditionnement(produit, capaciteBouteilles));
    }

    @Transactional
    public Conditionnement modifier(Long id, boolean actif) {
        Conditionnement conditionnement = trouver(id);
        conditionnement.setActif(actif);
        return repository.enregistrer(conditionnement);
    }

    @Transactional
    public void desactiver(Long id) {
        Conditionnement conditionnement = trouver(id);
        conditionnement.setActif(false);
        repository.enregistrer(conditionnement);
    }

    @Transactional(readOnly = true)
    public Conditionnement trouver(Long id) {
        return repository.parId(id)
                .orElseThrow(() -> new NoSuchElementException("Conditionnement introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public Page<Conditionnement> rechercher(Long produitId, Boolean actif, Pageable pageable) {
        return repository.rechercher(produitId, actif, pageable);
    }
}
