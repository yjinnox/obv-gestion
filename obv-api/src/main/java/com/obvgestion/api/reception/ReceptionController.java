package com.obvgestion.api.reception;

import com.obvgestion.api.PageResponse;
import com.obvgestion.application.reception.ReceptionService;
import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.reception.StatutReception;
import com.obvgestion.infrastructure.securite.JwtPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** §7, §13 — cycle de vie complet d'une réception au dépôt. */
@RestController
@RequestMapping("/api/v1/receptions")
class ReceptionController {

    private final ReceptionService service;

    ReceptionController(ReceptionService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('RECEPTION_READ', 'RECEPTION_WRITE')")
    PageResponse<ReceptionResponse> lister(@RequestParam(name = "pdv", required = false) Long pointDeVenteId,
                                            @RequestParam(required = false) StatutReception statut,
                                            Pageable pageable) {
        return PageResponse.de(service.rechercher(pointDeVenteId, statut, pageable), ReceptionResponse::de);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('RECEPTION_READ', 'RECEPTION_WRITE')")
    ReceptionResponse obtenir(@PathVariable Long id) {
        return ReceptionResponse.de(service.trouver(id));
    }

    @GetMapping("/{id}/recapitulatif")
    @PreAuthorize("hasAnyAuthority('RECEPTION_READ', 'RECEPTION_WRITE')")
    RecapitulatifReceptionResponse recapitulatif(@PathVariable Long id) {
        return RecapitulatifReceptionResponse.de(service.recapitulatif(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('RECEPTION_WRITE')")
    ReceptionResponse creer(@Valid @RequestBody CreerReceptionRequest requete) {
        return ReceptionResponse.de(
                service.creer(requete.fournisseurId(), requete.pointDeVenteId(), requete.dateHeureLivraison()));
    }

    @PostMapping("/{id}/lignes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('RECEPTION_WRITE')")
    ReceptionResponse ajouterLigne(@PathVariable Long id, @Valid @RequestBody AjouterLigneReceptionRequest requete) {
        Montant prix = requete.prixAchatCasierXof() == null ? null : new Montant(requete.prixAchatCasierXof());
        service.ajouterLigne(id, requete.produitId(), requete.conditionnementId(), requete.nombreCasiers(), prix);
        return ReceptionResponse.de(service.trouver(id));
    }

    @PatchMapping("/{id}/lignes/{ligneId}")
    @PreAuthorize("hasAuthority('RECEPTION_WRITE')")
    ReceptionResponse modifierLigne(@PathVariable Long id, @PathVariable Long ligneId,
                                     @Valid @RequestBody ModifierLigneReceptionRequest requete,
                                     Authentication authentication, HttpServletRequest requeteHttp) {
        service.modifierLigne(id, ligneId, requete.nombreCasiers(), new Montant(requete.prixAchatCasierXof()),
                principal(authentication).utilisateurId(), adresseIp(requeteHttp));
        return ReceptionResponse.de(service.trouver(id));
    }

    @DeleteMapping("/{id}/lignes/{ligneId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('RECEPTION_WRITE')")
    void supprimerLigne(@PathVariable Long id, @PathVariable Long ligneId) {
        service.supprimerLigne(id, ligneId);
    }

    @PostMapping("/{id}/cloturer")
    @PreAuthorize("hasAuthority('RECEPTION_WRITE')")
    ReceptionResponse cloturer(@PathVariable Long id, Authentication authentication) {
        return ReceptionResponse.de(service.cloturer(id, principal(authentication).utilisateurId()));
    }

    @PostMapping("/{id}/demander-validation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('RECEPTION_WRITE')")
    void demanderValidation(@PathVariable Long id, @Valid @RequestBody DemanderValidationRequest requete) {
        service.demanderValidation(id, requete.destinataireId());
    }

    @PostMapping("/{id}/valider")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('RECEPTION_VALIDER')")
    void valider(@PathVariable Long id, Authentication authentication, HttpServletRequest requeteHttp) {
        service.valider(id, principal(authentication).utilisateurId(), adresseIp(requeteHttp));
    }

    @PostMapping("/{id}/annuler")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('RECEPTION_VALIDER')")
    void annuler(@PathVariable Long id, @Valid @RequestBody AnnulerReceptionRequest requete,
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
