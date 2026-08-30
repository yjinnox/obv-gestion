package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.referentiel.VolumeRepository;
import com.obvgestion.domain.referentiel.Volume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class VolumeRepositoryAdapter implements VolumeRepository {

    private final VolumeJpaRepository jpaRepository;

    VolumeRepositoryAdapter(VolumeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Volume enregistrer(Volume volume) {
        return jpaRepository.save(volume);
    }

    @Override
    public Optional<Volume> parId(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Page<Volume> rechercher(Boolean actif, Pageable pageable) {
        return jpaRepository.rechercher(actif, pageable);
    }
}
