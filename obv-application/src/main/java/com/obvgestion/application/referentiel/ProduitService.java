package com.obvgestion.application.referentiel;

import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.referentiel.Marque;
import com.obvgestion.domain.referentiel.Produit;
import com.obvgestion.domain.referentiel.Volume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class ProduitService {

    private final ProduitRepository repository;
    private final MarqueRepository marqueRepository;
    private final VolumeRepository volumeRepository;

    public ProduitService(ProduitRepository repository, MarqueRepository marqueRepository,
                           VolumeRepository volumeRepository) {
        this.repository = repository;
        this.marqueRepository = marqueRepository;
        this.volumeRepository = volumeRepository;
    }

    @Transactional
    public Produit creer(Long marqueId, Long volumeId) {
        Marque marque = marqueRepository.parId(marqueId)
                .orElseThrow(() -> new NoSuchElementException("Marque introuvable : " + marqueId));
        Volume volume = volumeRepository.parId(volumeId)
                .orElseThrow(() -> new NoSuchElementException("Volume introuvable : " + volumeId));
        return repository.enregistrer(new Produit(marque, volume));
    }

    @Transactional
    public Produit modifier(Long id, Montant montantConsigne, boolean actif) {
        Produit produit = trouver(id);
        produit.setMontantConsigne(montantConsigne);
        produit.setActif(actif);
        return repository.enregistrer(produit);
    }

    @Transactional
    public void desactiver(Long id) {
        Produit produit = trouver(id);
        produit.setActif(false);
        repository.enregistrer(produit);
    }

    @Transactional(readOnly = true)
    public Produit trouver(Long id) {
        return repository.parId(id).orElseThrow(() -> new NoSuchElementException("Produit introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public Page<Produit> rechercher(Long marqueId, Long volumeId, Boolean actif, Pageable pageable) {
        return repository.rechercher(marqueId, volumeId, actif, pageable);
    }
}
