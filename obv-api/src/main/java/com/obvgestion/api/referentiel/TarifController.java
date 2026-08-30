package com.obvgestion.api.referentiel;

import com.obvgestion.api.PageResponse;
import com.obvgestion.application.referentiel.TarifService;
import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.referentiel.NatureTarif;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * §5.2, §13 — tarification datée. Ni PATCH ni DELETE : un tarif est
 * historisé, jamais modifié ni supprimé (RG-08/RG-09) — seule une nouvelle
 * création le clôt et le remplace.
 */
@RestController
@RequestMapping("/api/v1/tarifs")
class TarifController {

    private final TarifService service;

    TarifController(TarifService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REFERENTIEL_READ')")
    PageResponse<TarifResponse> lister(@RequestParam(required = false) Long pdv,
                                        @RequestParam(required = false) Long produit,
                                        @RequestParam(required = false) NatureTarif nature,
                                        Pageable pageable) {
        return PageResponse.de(service.rechercher(pdv, produit, nature, pageable), TarifResponse::de);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('REFERENTIEL_WRITE')")
    TarifResponse creer(@Valid @RequestBody CreerTarifRequest requete) {
        return TarifResponse.de(service.creer(requete.pointDeVenteId(), requete.produitId(), requete.uniteVente(),
                requete.nature(), new Montant(requete.montantXof()), requete.dateDebut()));
    }
}
