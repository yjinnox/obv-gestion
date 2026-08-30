package com.obvgestion.api.referentiel;

import com.obvgestion.api.PageResponse;
import com.obvgestion.application.referentiel.FournisseurService;
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

/** §5.1, §13 — référentiel des fournisseurs. */
@RestController
@RequestMapping("/api/v1/fournisseurs")
class FournisseurController {

    private final FournisseurService service;

    FournisseurController(FournisseurService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REFERENTIEL_READ')")
    PageResponse<FournisseurResponse> lister(@RequestParam(required = false) Boolean actif, Pageable pageable) {
        return PageResponse.de(service.rechercher(actif, pageable), FournisseurResponse::de);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REFERENTIEL_READ')")
    FournisseurResponse obtenir(@PathVariable Long id) {
        return FournisseurResponse.de(service.trouver(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    FournisseurResponse creer(@Valid @RequestBody CreerFournisseurRequest requete) {
        return FournisseurResponse.de(
                service.creer(requete.raisonSociale(), requete.telephone(), requete.email(), requete.adresse()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    FournisseurResponse modifier(@PathVariable Long id, @Valid @RequestBody ModifierFournisseurRequest requete) {
        return FournisseurResponse.de(service.modifier(id, requete.raisonSociale(), requete.telephone(),
                requete.email(), requete.adresse(), requete.actif()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    void desactiver(@PathVariable Long id) {
        service.desactiver(id);
    }
}
