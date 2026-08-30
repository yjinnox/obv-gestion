package com.obvgestion.api.referentiel;

import com.obvgestion.domain.referentiel.Volume;

public record VolumeResponse(Long id, String libelle, int contenanceMl, boolean actif) {

    public static VolumeResponse de(Volume volume) {
        return new VolumeResponse(volume.getId(), volume.getLibelle(), volume.getContenanceMl(), volume.isActif());
    }
}
