package com.obvgestion.api.bar;

import com.obvgestion.api.PageResponse;
import com.obvgestion.application.bar.TicketServeurService;
import com.obvgestion.domain.bar.StatutTicketServeur;
import com.obvgestion.domain.bar.TicketServeur;
import com.obvgestion.infrastructure.securite.JwtPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** §10, §13 — vente au bar/maquis par ticket serveur (RG-01, RG-33, RG-34). */
@RestController
@RequestMapping("/api/v1/tickets-serveur")
class TicketServeurController {

    private final TicketServeurService service;

    TicketServeurController(TicketServeurService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('VENTE_READ', 'VENTE_WRITE')")
    PageResponse<TicketServeurResponse> lister(@RequestParam(name = "sessionVenteId", required = false) Long sessionVenteId,
                                                @RequestParam(name = "serveurId", required = false) Long serveurId,
                                                @RequestParam(required = false) StatutTicketServeur statut,
                                                Pageable pageable) {
        return PageResponse.de(service.rechercher(sessionVenteId, serveurId, statut, pageable),
                TicketServeurResponse::de);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('VENTE_READ', 'VENTE_WRITE')")
    TicketServeurResponse obtenir(@PathVariable Long id) {
        return TicketServeurResponse.de(service.trouver(id));
    }

    /** RG-33 — total par serveur, par marque/volume, total général. */
    @GetMapping("/recapitulatif")
    @PreAuthorize("hasAnyAuthority('VENTE_READ', 'VENTE_WRITE')")
    RecapitulatifSessionBarResponse recapitulatif(@RequestParam Long sessionVenteId) {
        return RecapitulatifSessionBarResponse.de(service.recapitulatif(sessionVenteId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('VENTE_WRITE')")
    TicketServeurResponse creer(@Valid @RequestBody CreerTicketServeurRequest requete) {
        return TicketServeurResponse.de(service.creer(requete.sessionVenteId(), requete.serveurId()));
    }

    @PostMapping("/{id}/lignes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('VENTE_WRITE')")
    TicketServeurResponse ajouterLigne(@PathVariable Long id, @Valid @RequestBody AjouterLigneTicketRequest requete) {
        service.ajouterLigne(id, requete.produitId(), requete.quantiteBouteilles(), requete.prixVenteBouteilleXof());
        return TicketServeurResponse.de(service.trouver(id));
    }

    @PostMapping("/{id}/encaisser")
    @PreAuthorize("hasAuthority('VENTE_WRITE')")
    TicketServeurResponse encaisser(@PathVariable Long id, @Valid @RequestBody EncaisserTicketRequest requete,
                                     Authentication authentication) {
        TicketServeur ticket = service.encaisser(id, requete.modePaiement(), principal(authentication).utilisateurId());
        return TicketServeurResponse.de(ticket);
    }

    /** RG-29 (par analogie) — correction d'une quantité vendue, réservée au SUPER_ADMINISTRATEUR pendant EN_MODIFICATION. */
    @PatchMapping("/{id}/lignes/{ligneId}")
    @PreAuthorize("hasAuthority('MODIFICATION_POST_CLOTURE')")
    TicketServeurResponse modifierLigne(@PathVariable Long id, @PathVariable Long ligneId,
                                         @Valid @RequestBody ModifierLigneTicketRequest requete,
                                         Authentication authentication, HttpServletRequest requeteHttp) {
        service.modifierQuantiteLigne(id, ligneId, requete.quantiteBouteilles(),
                principal(authentication).utilisateurId(), requeteHttp.getRemoteAddr());
        return TicketServeurResponse.de(service.trouver(id));
    }

    private static JwtPrincipal principal(Authentication authentication) {
        return (JwtPrincipal) authentication.getPrincipal();
    }
}
