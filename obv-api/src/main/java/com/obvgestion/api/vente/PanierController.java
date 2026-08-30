package com.obvgestion.api.vente;

import com.obvgestion.application.vente.PanierService;
import com.obvgestion.infrastructure.securite.JwtPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

/** §8.2, §13 — panier de vente au dépôt (Redis, propre à chaque utilisateur et session). */
@RestController
@RequestMapping("/api/v1/panier")
class PanierController {

    private final PanierService service;

    PanierController(PanierService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VENTE_WRITE')")
    PanierResponse obtenir(@RequestParam Long sessionVenteId, Authentication authentication) {
        return PanierResponse.de(service.obtenirDetaille(principal(authentication).utilisateurId(), sessionVenteId));
    }

    @PostMapping("/lignes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('VENTE_WRITE')")
    AjoutPanierResponse ajouterLigne(@Valid @RequestBody AjouterLignePanierRequest requete,
                                      Authentication authentication) {
        return AjoutPanierResponse.de(service.ajouterLigne(principal(authentication).utilisateurId(),
                requete.sessionVenteId(), requete.produitId(), requete.quantiteDemiCasiers()));
    }

    @PatchMapping("/lignes/{ligneId}")
    @PreAuthorize("hasAuthority('VENTE_WRITE')")
    PanierResponse modifierLigne(@PathVariable int ligneId, @Valid @RequestBody ModifierLignePanierRequest requete,
                                  Authentication authentication) {
        service.modifierLigne(principal(authentication).utilisateurId(), requete.sessionVenteId(), ligneId,
                requete.quantiteDemiCasiers());
        return PanierResponse.de(
                service.obtenirDetaille(principal(authentication).utilisateurId(), requete.sessionVenteId()));
    }

    @DeleteMapping("/lignes/{ligneId}")
    @PreAuthorize("hasAuthority('VENTE_WRITE')")
    PanierResponse supprimerLigne(@PathVariable int ligneId, @RequestParam Long sessionVenteId,
                                   Authentication authentication) {
        service.supprimerLigne(principal(authentication).utilisateurId(), sessionVenteId, ligneId);
        return PanierResponse.de(service.obtenirDetaille(principal(authentication).utilisateurId(), sessionVenteId));
    }

    /** RG-25 — aucun effet sur le stock ni la base. */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('VENTE_WRITE')")
    void vider(@RequestParam Long sessionVenteId, Authentication authentication) {
        service.vider(principal(authentication).utilisateurId(), sessionVenteId);
    }

    private static JwtPrincipal principal(Authentication authentication) {
        return (JwtPrincipal) authentication.getPrincipal();
    }
}
