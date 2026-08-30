package com.obvgestion.application.referentiel;

import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.referentiel.TypePointDeVente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class PointDeVenteService {

    private final PointDeVenteRepository repository;

    public PointDeVenteService(PointDeVenteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PointDeVente creer(String libelle, TypePointDeVente type, String adresse) {
        return repository.enregistrer(new PointDeVente(libelle, type, adresse));
    }

    /** Le type (DEPOT/BAR) est immuable après création : il conditionne rôles et tarifs déjà rattachés. */
    @Transactional
    public PointDeVente modifier(Long id, String libelle, String adresse, boolean actif) {
        PointDeVente pointDeVente = trouver(id);
        pointDeVente.setLibelle(libelle);
        pointDeVente.setAdresse(adresse);
        pointDeVente.setActif(actif);
        return repository.enregistrer(pointDeVente);
    }

    @Transactional
    public void desactiver(Long id) {
        PointDeVente pointDeVente = trouver(id);
        pointDeVente.setActif(false);
        repository.enregistrer(pointDeVente);
    }

    @Transactional(readOnly = true)
    public PointDeVente trouver(Long id) {
        return repository.parId(id)
                .orElseThrow(() -> new NoSuchElementException("Point de vente introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public Page<PointDeVente> rechercher(Boolean actif, Pageable pageable) {
        return repository.rechercher(actif, pageable);
    }
}
