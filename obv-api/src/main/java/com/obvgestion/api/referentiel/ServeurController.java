package com.obvgestion.api.referentiel;

import com.obvgestion.api.PageResponse;
import com.obvgestion.application.referentiel.ServeurService;
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

/** §5.1, §13 — référentiel des serveurs d'un bar/maquis. */
@RestController
@RequestMapping("/api/v1/serveurs")
class ServeurController {

    private final ServeurService service;

    ServeurController(ServeurService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REFERENTIEL_READ')")
    PageResponse<ServeurResponse> lister(@RequestParam(required = false) Long pdv,
                                          @RequestParam(required = false) Boolean actif,
                                          Pageable pageable) {
        return PageResponse.de(service.rechercher(pdv, actif, pageable), ServeurResponse::de);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REFERENTIEL_READ')")
    ServeurResponse obtenir(@PathVariable Long id) {
        return ServeurResponse.de(service.trouver(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    ServeurResponse creer(@Valid @RequestBody CreerServeurRequest requete) {
        return ServeurResponse.de(
                service.creer(requete.pointDeVenteId(), requete.nom(), requete.prenoms(), requete.telephone()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    ServeurResponse modifier(@PathVariable Long id, @Valid @RequestBody ModifierServeurRequest requete) {
        return ServeurResponse.de(
                service.modifier(id, requete.nom(), requete.prenoms(), requete.telephone(), requete.actif()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    void desactiver(@PathVariable Long id) {
        service.desactiver(id);
    }
}
