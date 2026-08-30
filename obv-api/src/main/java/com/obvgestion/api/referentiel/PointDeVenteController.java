package com.obvgestion.api.referentiel;

import com.obvgestion.api.PageResponse;
import com.obvgestion.application.referentiel.PointDeVenteService;
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

/** §1, §5.1, §13 — référentiel des points de vente (dépôts, bars/maquis). */
@RestController
@RequestMapping("/api/v1/points-de-vente")
class PointDeVenteController {

    private final PointDeVenteService service;

    PointDeVenteController(PointDeVenteService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REFERENTIEL_READ')")
    PageResponse<PointDeVenteResponse> lister(@RequestParam(required = false) Boolean actif, Pageable pageable) {
        return PageResponse.de(service.rechercher(actif, pageable), PointDeVenteResponse::de);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REFERENTIEL_READ')")
    PointDeVenteResponse obtenir(@PathVariable Long id) {
        return PointDeVenteResponse.de(service.trouver(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    PointDeVenteResponse creer(@Valid @RequestBody CreerPointDeVenteRequest requete) {
        return PointDeVenteResponse.de(service.creer(requete.libelle(), requete.type(), requete.adresse()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    PointDeVenteResponse modifier(@PathVariable Long id, @Valid @RequestBody ModifierPointDeVenteRequest requete) {
        return PointDeVenteResponse.de(service.modifier(id, requete.libelle(), requete.adresse(), requete.actif()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    void desactiver(@PathVariable Long id) {
        service.desactiver(id);
    }
}
