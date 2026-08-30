package com.obvgestion.api.utilisateur;

import com.obvgestion.api.PageResponse;
import com.obvgestion.application.utilisateur.CreationUtilisateurCommande;
import com.obvgestion.application.utilisateur.UtilisateurService;
import com.obvgestion.domain.utilisateur.RoleUtilisateur;
import com.obvgestion.domain.utilisateur.StatutUtilisateur;
import com.obvgestion.domain.utilisateur.Utilisateur;
import com.obvgestion.infrastructure.securite.JwtPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
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

/** §4.1, §4.3 — création et administration des comptes utilisateurs. */
@RestController
@RequestMapping("/api/v1/utilisateurs")
class UtilisateurController {

    private final UtilisateurService utilisateurService;

    UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('UTILISATEUR_READ')")
    PageResponse<UtilisateurResponse> lister(@RequestParam(required = false) StatutUtilisateur statut,
                                              @RequestParam(required = false) RoleUtilisateur role,
                                              @RequestParam(required = false) Long pdv,
                                              @RequestParam(required = false) String recherche,
                                              Pageable pageable) {
        return PageResponse.de(
                utilisateurService.rechercher(statut, role, pdv, recherche, pageable), UtilisateurResponse::de);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('UTILISATEUR_WRITE')")
    UtilisateurResponse creer(@Valid @RequestBody CreerUtilisateurRequest requete) {
        Utilisateur utilisateur = utilisateurService.creer(new CreationUtilisateurCommande(
                requete.nom(), requete.prenoms(), requete.canalContact(), requete.email(), requete.telephone(),
                requete.affectations().stream()
                        .map(a -> new CreationUtilisateurCommande.AffectationCommande(a.role(), a.pointDeVenteId()))
                        .toList()));
        return UtilisateurResponse.de(utilisateur);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('UTILISATEUR_WRITE')")
    UtilisateurResponse modifier(@PathVariable Long id, @Valid @RequestBody ModifierUtilisateurRequest requete) {
        return UtilisateurResponse.de(utilisateurService.renommer(id, requete.nom(), requete.prenoms()));
    }

    @PostMapping("/{id}/activer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('UTILISATEUR_WRITE')")
    void activer(@PathVariable Long id, Authentication authentication, HttpServletRequest requeteHttp) {
        utilisateurService.reactiver(id, acteur(authentication), adresseIp(requeteHttp));
    }

    @PostMapping("/{id}/desactiver")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('UTILISATEUR_WRITE')")
    void desactiver(@PathVariable Long id, Authentication authentication, HttpServletRequest requeteHttp) {
        utilisateurService.desactiver(id, principal(authentication).utilisateurId(), acteur(authentication),
                adresseIp(requeteHttp));
    }

    @PostMapping("/{id}/archiver")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('UTILISATEUR_WRITE')")
    void archiver(@PathVariable Long id, Authentication authentication, HttpServletRequest requeteHttp) {
        utilisateurService.archiver(id, principal(authentication).utilisateurId(), acteur(authentication),
                adresseIp(requeteHttp));
    }

    @PostMapping("/{id}/reinitialiser-mot-de-passe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('UTILISATEUR_WRITE')")
    void reinitialiserMotDePasse(@PathVariable Long id) {
        utilisateurService.reinitialiserMotDePasse(id);
    }

    @PostMapping("/{id}/affectations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('UTILISATEUR_WRITE')")
    void ajouterAffectation(@PathVariable Long id, @Valid @RequestBody AffectationRequest requete,
                             Authentication authentication, HttpServletRequest requeteHttp) {
        utilisateurService.ajouterAffectation(id, requete.role(), requete.pointDeVenteId(), acteur(authentication),
                adresseIp(requeteHttp));
    }

    @DeleteMapping("/{id}/affectations/{affectationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('UTILISATEUR_WRITE')")
    void retirerAffectation(@PathVariable Long id, @PathVariable Long affectationId, Authentication authentication,
                             HttpServletRequest requeteHttp) {
        utilisateurService.retirerAffectation(id, affectationId, principal(authentication).utilisateurId(),
                acteur(authentication), adresseIp(requeteHttp));
    }

    private static JwtPrincipal principal(Authentication authentication) {
        return (JwtPrincipal) authentication.getPrincipal();
    }

    private static String acteur(Authentication authentication) {
        return principal(authentication).utilisateurId().toString();
    }

    private static String adresseIp(HttpServletRequest requete) {
        return requete.getRemoteAddr();
    }
}
