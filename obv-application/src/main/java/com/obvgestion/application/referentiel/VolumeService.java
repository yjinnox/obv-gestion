package com.obvgestion.application.referentiel;

import com.obvgestion.domain.referentiel.Volume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class VolumeService {

    private final VolumeRepository repository;

    public VolumeService(VolumeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Volume creer(String libelle, int contenanceMl) {
        return repository.enregistrer(new Volume(libelle, contenanceMl));
    }

    @Transactional
    public Volume modifier(Long id, String libelle, int contenanceMl, boolean actif) {
        Volume volume = trouver(id);
        volume.setLibelle(libelle);
        volume.setContenanceMl(contenanceMl);
        volume.setActif(actif);
        return repository.enregistrer(volume);
    }

    @Transactional
    public void desactiver(Long id) {
        Volume volume = trouver(id);
        volume.setActif(false);
        repository.enregistrer(volume);
    }

    @Transactional(readOnly = true)
    public Volume trouver(Long id) {
        return repository.parId(id).orElseThrow(() -> new NoSuchElementException("Volume introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public Page<Volume> rechercher(Boolean actif, Pageable pageable) {
        return repository.rechercher(actif, pageable);
    }
}
