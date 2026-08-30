package com.obvgestion.application.referentiel;

import com.obvgestion.domain.referentiel.Fournisseur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class FournisseurService {

    private final FournisseurRepository repository;

    public FournisseurService(FournisseurRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Fournisseur creer(String raisonSociale, String telephone, String email, String adresse) {
        return repository.enregistrer(new Fournisseur(raisonSociale, telephone, email, adresse));
    }

    @Transactional
    public Fournisseur modifier(Long id, String raisonSociale, String telephone, String email, String adresse,
                                 boolean actif) {
        Fournisseur fournisseur = trouver(id);
        fournisseur.setRaisonSociale(raisonSociale);
        fournisseur.setTelephone(telephone);
        fournisseur.setEmail(email);
        fournisseur.setAdresse(adresse);
        fournisseur.setActif(actif);
        return repository.enregistrer(fournisseur);
    }

    @Transactional
    public void desactiver(Long id) {
        Fournisseur fournisseur = trouver(id);
        fournisseur.setActif(false);
        repository.enregistrer(fournisseur);
    }

    @Transactional(readOnly = true)
    public Fournisseur trouver(Long id) {
        return repository.parId(id)
                .orElseThrow(() -> new NoSuchElementException("Fournisseur introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public Page<Fournisseur> rechercher(Boolean actif, Pageable pageable) {
        return repository.rechercher(actif, pageable);
    }
}
