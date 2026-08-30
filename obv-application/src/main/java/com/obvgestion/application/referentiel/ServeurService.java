package com.obvgestion.application.referentiel;

import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.referentiel.Serveur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class ServeurService {

    private final ServeurRepository repository;
    private final PointDeVenteRepository pointDeVenteRepository;

    public ServeurService(ServeurRepository repository, PointDeVenteRepository pointDeVenteRepository) {
        this.repository = repository;
        this.pointDeVenteRepository = pointDeVenteRepository;
    }

    @Transactional
    public Serveur creer(Long pointDeVenteId, String nom, String prenoms, String telephone) {
        PointDeVente pointDeVente = pointDeVenteRepository.parId(pointDeVenteId)
                .orElseThrow(() -> new NoSuchElementException("Point de vente introuvable : " + pointDeVenteId));
        return repository.enregistrer(new Serveur(pointDeVente, nom, prenoms, telephone));
    }

    @Transactional
    public Serveur modifier(Long id, String nom, String prenoms, String telephone, boolean actif) {
        Serveur serveur = trouver(id);
        serveur.setNom(nom);
        serveur.setPrenoms(prenoms);
        serveur.setTelephone(telephone);
        serveur.setActif(actif);
        return repository.enregistrer(serveur);
    }

    @Transactional
    public void desactiver(Long id) {
        Serveur serveur = trouver(id);
        serveur.setActif(false);
        repository.enregistrer(serveur);
    }

    @Transactional(readOnly = true)
    public Serveur trouver(Long id) {
        return repository.parId(id).orElseThrow(() -> new NoSuchElementException("Serveur introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public Page<Serveur> rechercher(Long pointDeVenteId, Boolean actif, Pageable pageable) {
        return repository.rechercher(pointDeVenteId, actif, pageable);
    }
}
