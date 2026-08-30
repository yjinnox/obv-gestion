package com.obvgestion.api.vente;

import com.obvgestion.api.PageResponse;
import com.obvgestion.application.vente.SessionVenteService;
import com.obvgestion.domain.commun.Montant;
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

/** §8.1, §8.3, §13 — cycle de vie de la session de vente. */
@RestController
@RequestMapping("/api/v1/sessions-vente")
class SessionVenteController {

    private final SessionVenteService service;

    SessionVenteController(SessionVenteService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SESSION_CLOTURER', 'SESSION_VALIDER')")
    PageResponse<SessionVenteResponse> lister(@RequestParam(name = "pdv", required = false) Long pointDeVenteId,
                                               Pageable pageable) {
        return PageResponse.de(service.rechercher(pointDeVenteId, pageable), SessionVenteResponse::de);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SESSION_CLOTURER', 'SESSION_VALIDER')")
    SessionVenteResponse obtenir(@PathVariable Long id) {
        return SessionVenteResponse.de(service.trouver(id));
    }

    @GetMapping("/courante")
    @PreAuthorize("hasAnyAuthority('SESSION_CLOTURER', 'SESSION_VALIDER')")
    SessionVenteResponse courante(@RequestParam(name = "pdv") Long pointDeVenteId) {
        return SessionVenteResponse.de(service.courante(pointDeVenteId));
    }

    @GetMapping("/{id}/recapitulatif")
    @PreAuthorize("hasAnyAuthority('SESSION_CLOTURER', 'SESSION_VALIDER')")
    RecapitulatifSessionVenteResponse recapitulatif(@PathVariable Long id) {
        return RecapitulatifSessionVenteResponse.de(service.recapitulatif(id));
    }

    @PostMapping("/ouvrir")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SESSION_CLOTURER')")
    SessionVenteResponse ouvrir(@Valid @RequestBody OuvrirSessionVenteRequest requete, Authentication authentication) {
        return SessionVenteResponse.de(service.ouvrir(requete.pointDeVenteId(), principal(authentication).utilisateurId(),
                new Montant(requete.fondCaisseXof())));
    }

    @PostMapping("/{id}/cloturer")
    @PreAuthorize("hasAuthority('SESSION_CLOTURER')")
    SessionVenteResponse cloturer(@PathVariable Long id, @Valid @RequestBody ClotureSessionVenteRequest requete,
                                   Authentication authentication) {
        return SessionVenteResponse.de(service.cloturer(id, principal(authentication).utilisateurId(),
                new Montant(requete.totalCompteXof())));
    }

    @PostMapping("/{id}/valider")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('SESSION_VALIDER')")
    void valider(@PathVariable Long id, Authentication authentication, HttpServletRequest requeteHttp) {
        service.valider(id, principal(authentication).utilisateurId(), requeteHttp.getRemoteAddr());
    }

    @PostMapping("/{id}/demander-modification")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('SESSION_CLOTURER')")
    void demanderModification(@PathVariable Long id) {
        service.demanderModification(id);
    }

    private static JwtPrincipal principal(Authentication authentication) {
        return (JwtPrincipal) authentication.getPrincipal();
    }
}
