package com.obvgestion.application.referentiel;

import com.obvgestion.domain.referentiel.Volume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface VolumeRepository {

    Volume enregistrer(Volume volume);

    Optional<Volume> parId(Long id);

    Page<Volume> rechercher(Boolean actif, Pageable pageable);
}
