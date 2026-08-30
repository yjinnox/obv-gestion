package com.obvgestion.api.referentiel;

import com.obvgestion.api.PageResponse;
import com.obvgestion.application.referentiel.ClientService;
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

/** §5.1, §13 — clients du dépôt (RG-07). */
@RestController
@RequestMapping("/api/v1/clients")
class ClientController {

    private final ClientService service;

    ClientController(ClientService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CLIENT_READ')")
    PageResponse<ClientResponse> lister(@RequestParam(required = false) Boolean actif,
                                         @RequestParam(required = false) String recherche,
                                         Pageable pageable) {
        return PageResponse.de(service.rechercher(actif, recherche, pageable), ClientResponse::de);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENT_READ')")
    ClientResponse obtenir(@PathVariable Long id) {
        return ClientResponse.de(service.trouver(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CLIENT_WRITE')")
    ClientResponse creer(@Valid @RequestBody CreerClientRequest requete) {
        return ClientResponse.de(service.creer(requete.type(), requete.nom(), requete.prenoms(),
                requete.raisonSociale(), requete.telephone(), requete.email(), requete.adresseFacturation()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENT_WRITE')")
    ClientResponse modifier(@PathVariable Long id, @Valid @RequestBody ModifierClientRequest requete) {
        return ClientResponse.de(service.modifier(id, requete.telephone(), requete.email(),
                requete.adresseFacturation(), requete.actif()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('CLIENT_WRITE')")
    void desactiver(@PathVariable Long id) {
        service.desactiver(id);
    }
}
