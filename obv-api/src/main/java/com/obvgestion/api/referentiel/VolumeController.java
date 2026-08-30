package com.obvgestion.api.referentiel;

import com.obvgestion.api.PageResponse;
import com.obvgestion.application.referentiel.VolumeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** §5.1, §13 — référentiel des volumes. */
@RestController
@RequestMapping("/api/v1/volumes")
class VolumeController {

    private final VolumeService service;

    VolumeController(VolumeService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REFERENTIEL_READ')")
    PageResponse<VolumeResponse> lister(@RequestParam(required = false) Boolean actif, Pageable pageable) {
        return PageResponse.de(service.rechercher(actif, pageable), VolumeResponse::de);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REFERENTIEL_READ')")
    VolumeResponse obtenir(@PathVariable Long id) {
        return VolumeResponse.de(service.trouver(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    VolumeResponse creer(@Valid @RequestBody CreerVolumeRequest requete) {
        return VolumeResponse.de(service.creer(requete.libelle(), requete.contenanceMl()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    VolumeResponse modifier(@PathVariable Long id, @Valid @RequestBody ModifierVolumeRequest requete) {
        return VolumeResponse.de(service.modifier(id, requete.libelle(), requete.contenanceMl(), requete.actif()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    void desactiver(@PathVariable Long id) {
        service.desactiver(id);
    }
}
