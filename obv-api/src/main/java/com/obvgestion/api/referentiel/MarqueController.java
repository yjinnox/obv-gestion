package com.obvgestion.api.referentiel;

import com.obvgestion.api.PageResponse;
import com.obvgestion.application.referentiel.MarqueService;
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

/** §5.1, §13 — référentiel des marques. */
@RestController
@RequestMapping("/api/v1/marques")
class MarqueController {

    private final MarqueService service;

    MarqueController(MarqueService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REFERENTIEL_READ')")
    PageResponse<MarqueResponse> lister(@RequestParam(required = false) Boolean actif, Pageable pageable) {
        return PageResponse.de(service.rechercher(actif, pageable), MarqueResponse::de);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REFERENTIEL_READ')")
    MarqueResponse obtenir(@PathVariable Long id) {
        return MarqueResponse.de(service.trouver(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    MarqueResponse creer(@Valid @RequestBody CreerMarqueRequest requete) {
        return MarqueResponse.de(service.creer(requete.libelle()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    MarqueResponse modifier(@PathVariable Long id, @Valid @RequestBody ModifierMarqueRequest requete) {
        return MarqueResponse.de(service.modifier(id, requete.libelle(), requete.actif()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    void desactiver(@PathVariable Long id) {
        service.desactiver(id);
    }
}
