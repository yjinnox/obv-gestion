package com.obvgestion.api.referentiel;

import com.obvgestion.api.PageResponse;
import com.obvgestion.application.referentiel.ConditionnementService;
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

/** §1, §5.1, §13 — référentiel des conditionnements (casiers). */
@RestController
@RequestMapping("/api/v1/conditionnements")
class ConditionnementController {

    private final ConditionnementService service;

    ConditionnementController(ConditionnementService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REFERENTIEL_READ')")
    PageResponse<ConditionnementResponse> lister(@RequestParam(required = false) Long produit,
                                                  @RequestParam(required = false) Boolean actif,
                                                  Pageable pageable) {
        return PageResponse.de(service.rechercher(produit, actif, pageable), ConditionnementResponse::de);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REFERENTIEL_READ')")
    ConditionnementResponse obtenir(@PathVariable Long id) {
        return ConditionnementResponse.de(service.trouver(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    ConditionnementResponse creer(@Valid @RequestBody CreerConditionnementRequest requete) {
        return ConditionnementResponse.de(service.creer(requete.produitId(), requete.capaciteBouteilles()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    ConditionnementResponse modifier(@PathVariable Long id,
                                      @Valid @RequestBody ModifierConditionnementRequest requete) {
        return ConditionnementResponse.de(service.modifier(id, requete.actif()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    void desactiver(@PathVariable Long id) {
        service.desactiver(id);
    }
}
