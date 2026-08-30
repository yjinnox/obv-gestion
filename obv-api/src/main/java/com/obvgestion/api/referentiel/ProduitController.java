package com.obvgestion.api.referentiel;

import com.obvgestion.api.PageResponse;
import com.obvgestion.application.referentiel.ProduitService;
import com.obvgestion.domain.commun.Montant;
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

/** §1, §5.1, §13 — référentiel des produits (couple marque + volume). */
@RestController
@RequestMapping("/api/v1/produits")
class ProduitController {

    private final ProduitService service;

    ProduitController(ProduitService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REFERENTIEL_READ')")
    PageResponse<ProduitResponse> lister(@RequestParam(required = false) Long marque,
                                          @RequestParam(required = false) Long volume,
                                          @RequestParam(required = false) Boolean actif,
                                          Pageable pageable) {
        return PageResponse.de(service.rechercher(marque, volume, actif, pageable), ProduitResponse::de);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REFERENTIEL_READ')")
    ProduitResponse obtenir(@PathVariable Long id) {
        return ProduitResponse.de(service.trouver(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    ProduitResponse creer(@Valid @RequestBody CreerProduitRequest requete) {
        return ProduitResponse.de(service.creer(requete.marqueId(), requete.volumeId()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    ProduitResponse modifier(@PathVariable Long id, @Valid @RequestBody ModifierProduitRequest requete) {
        return ProduitResponse.de(
                service.modifier(id, new Montant(requete.montantConsigneXof()), requete.actif()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    void desactiver(@PathVariable Long id) {
        service.desactiver(id);
    }
}
