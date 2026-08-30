package com.obvgestion.api.vente;

import com.obvgestion.api.PageResponse;
import com.obvgestion.application.vente.DocumentVentePdfGenerator;
import com.obvgestion.application.vente.NouveauClientCommande;
import com.obvgestion.application.vente.SessionVenteService;
import com.obvgestion.application.vente.VenteService;
import com.obvgestion.domain.vente.Vente;
import com.obvgestion.infrastructure.securite.JwtPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** §8.2, §13 — commande née de la validation du panier (entité {@code Vente}, RG-24 à RG-27, RG-29). */
@RestController
@RequestMapping("/api/v1/commandes")
class VenteController {

    private final VenteService venteService;
    private final SessionVenteService sessionVenteService;
    private final DocumentVentePdfGenerator pdfGenerator;
    private final int tauxTvaPourcent;

    VenteController(VenteService venteService, SessionVenteService sessionVenteService,
                     DocumentVentePdfGenerator pdfGenerator,
                     @Value("${entreprise.taux-tva-pourcent}") int tauxTvaPourcent) {
        this.venteService = venteService;
        this.sessionVenteService = sessionVenteService;
        this.pdfGenerator = pdfGenerator;
        this.tauxTvaPourcent = tauxTvaPourcent;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('VENTE_READ', 'VENTE_WRITE')")
    PageResponse<VenteResponse> lister(@RequestParam(required = false) Long sessionVenteId, Pageable pageable) {
        return PageResponse.de(venteService.rechercher(sessionVenteId, pageable), VenteResponse::de);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('VENTE_READ', 'VENTE_WRITE')")
    VenteResponse obtenir(@PathVariable Long id) {
        return VenteResponse.de(venteService.trouver(id));
    }

    /** RG-27 — idempotente sur l'en-tête {@code Idempotency-Key}. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('VENTE_WRITE')")
    VenteResponse commander(@Valid @RequestBody CreerCommandeRequest requete,
                             @RequestHeader("Idempotency-Key") String idempotencyKey, Authentication authentication) {
        NouveauClientCommande nouveauClient = requete.nouveauClient() == null ? null : new NouveauClientCommande(
                requete.nouveauClient().type(), requete.nouveauClient().nom(), requete.nouveauClient().prenoms(),
                requete.nouveauClient().raisonSociale(), requete.nouveauClient().telephone(),
                requete.nouveauClient().email(), requete.nouveauClient().adresseFacturation());
        Vente vente = venteService.commander(requete.sessionVenteId(), principal(authentication).utilisateurId(),
                requete.clientId(), nouveauClient, requete.modePaiement(), idempotencyKey);
        return VenteResponse.de(vente);
    }

    /** RG-29 — correction d'une quantité vendue, réservée au SUPER_ADMINISTRATEUR pendant EN_MODIFICATION. */
    @PatchMapping("/{id}/lignes/{ligneId}")
    @PreAuthorize("hasAuthority('MODIFICATION_POST_CLOTURE')")
    VenteResponse modifierQuantiteLigne(@PathVariable Long id, @PathVariable Long ligneId,
                                         @Valid @RequestBody ModifierQuantiteLigneVenteRequest requete,
                                         Authentication authentication, HttpServletRequest requeteHttp) {
        Vente vente = venteService.trouver(id);
        sessionVenteService.modifierQuantiteVente(id, ligneId, requete.quantiteDemiCasiers(), tauxTvaPourcent,
                principal(authentication).utilisateurId(), requeteHttp.getRemoteAddr());
        return VenteResponse.de(venteService.trouver(vente.getId()));
    }

    @GetMapping("/{id}/bon-de-commande.pdf")
    @PreAuthorize("hasAnyAuthority('VENTE_READ', 'VENTE_WRITE')")
    ResponseEntity<byte[]> bonDeCommande(@PathVariable Long id) {
        Vente vente = venteService.trouver(id);
        return pdf(pdfGenerator.genererBonDeCommande(vente), vente.getNumeroBonCommande());
    }

    @GetMapping("/{id}/facture.pdf")
    @PreAuthorize("hasAnyAuthority('VENTE_READ', 'VENTE_WRITE')")
    ResponseEntity<byte[]> facture(@PathVariable Long id) {
        Vente vente = venteService.trouver(id);
        return pdf(pdfGenerator.genererFacture(vente), vente.getNumeroFacture());
    }

    private static ResponseEntity<byte[]> pdf(byte[] contenu, String nomDocument) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomDocument + ".pdf\"")
                .body(contenu);
    }

    private static JwtPrincipal principal(Authentication authentication) {
        return (JwtPrincipal) authentication.getPrincipal();
    }
}
