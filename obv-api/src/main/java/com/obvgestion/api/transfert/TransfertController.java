package com.obvgestion.api.transfert;

import com.obvgestion.api.PageResponse;
import com.obvgestion.application.transfert.LigneTransfertDemandee;
import com.obvgestion.application.transfert.TransfertService;
import com.obvgestion.domain.transfert.BonTransfert;
import com.obvgestion.domain.transfert.StatutTransfert;
import com.obvgestion.infrastructure.securite.JwtPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** §9, §13 — cycle de vie complet d'un transfert dépôt → bar. */
@RestController
@RequestMapping("/api/v1/transferts")
class TransfertController {

    private final TransfertService service;

    TransfertController(TransfertService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TRANSFERT_WRITE', 'TRANSFERT_VALIDER')")
    PageResponse<BonTransfertResponse> lister(@RequestParam(name = "source", required = false) Long pointDeVenteSourceId,
                                               @RequestParam(name = "destination", required = false) Long pointDeVenteDestinationId,
                                               @RequestParam(required = false) StatutTransfert statut,
                                               Pageable pageable) {
        return PageResponse.de(service.rechercher(pointDeVenteSourceId, pointDeVenteDestinationId, statut, pageable),
                BonTransfertResponse::de);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TRANSFERT_WRITE', 'TRANSFERT_VALIDER')")
    BonTransfertResponse obtenir(@PathVariable Long id) {
        return BonTransfertResponse.de(service.trouver(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('TRANSFERT_WRITE')")
    BonTransfertResponse creer(@Valid @RequestBody CreerBonTransfertRequest requete) {
        var lignes = requete.lignes().stream()
                .map(l -> new LigneTransfertDemandee(l.produitId(), l.conditionnementId(), l.quantiteDemiCasiers(),
                        l.prixCessionCasierXof()))
                .toList();
        BonTransfert transfert = service.creer(requete.pointDeVenteSourceId(), requete.pointDeVenteDestinationId(),
                requete.dateHeure(), lignes);
        return BonTransfertResponse.de(transfert);
    }

    @PostMapping("/{id}/cloturer")
    @PreAuthorize("hasAuthority('TRANSFERT_WRITE')")
    BonTransfertResponse cloturer(@PathVariable Long id, Authentication authentication) {
        return BonTransfertResponse.de(service.cloturer(id, principal(authentication).utilisateurId()));
    }

    @PostMapping("/{id}/valider")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('TRANSFERT_VALIDER')")
    void valider(@PathVariable Long id, Authentication authentication, HttpServletRequest requeteHttp) {
        service.valider(id, principal(authentication).utilisateurId(), adresseIp(requeteHttp));
    }

    @PostMapping("/{id}/annuler")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('TRANSFERT_VALIDER')")
    void annuler(@PathVariable Long id, @Valid @RequestBody AnnulerTransfertRequest requete,
                 Authentication authentication, HttpServletRequest requeteHttp) {
        service.annuler(id, requete.motif(), principal(authentication).utilisateurId(), adresseIp(requeteHttp));
    }

    private static JwtPrincipal principal(Authentication authentication) {
        return (JwtPrincipal) authentication.getPrincipal();
    }

    private static String adresseIp(HttpServletRequest requete) {
        return requete.getRemoteAddr();
    }
}
