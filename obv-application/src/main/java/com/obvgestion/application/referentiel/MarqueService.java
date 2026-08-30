package com.obvgestion.application.referentiel;

import com.obvgestion.domain.referentiel.Marque;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class MarqueService {

    private final MarqueRepository repository;

    public MarqueService(MarqueRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Marque creer(String libelle) {
        return repository.enregistrer(new Marque(libelle));
    }

    @Transactional
    public Marque modifier(Long id, String libelle, boolean actif) {
        Marque marque = trouver(id);
        marque.setLibelle(libelle);
        marque.setActif(actif);
        return repository.enregistrer(marque);
    }

    @Transactional
    public void desactiver(Long id) {
        Marque marque = trouver(id);
        marque.setActif(false);
        repository.enregistrer(marque);
    }

    @Transactional(readOnly = true)
    public Marque trouver(Long id) {
        return repository.parId(id).orElseThrow(() -> new NoSuchElementException("Marque introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public Page<Marque> rechercher(Boolean actif, Pageable pageable) {
        return repository.rechercher(actif, pageable);
    }
}
