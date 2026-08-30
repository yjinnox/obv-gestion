package com.obvgestion.api.auth;

import com.obvgestion.application.utilisateur.ActivationService;
import com.obvgestion.application.utilisateur.AuthenticationService;
import com.obvgestion.application.utilisateur.ConnexionResultat;
import com.obvgestion.domain.utilisateur.MotDePasseInvalideException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

/** §4.4, §4.2 — authentification et activation des comptes. */
@RestController
@RequestMapping("/api/v1/auth")
class AuthController {

    private final AuthenticationService authenticationService;
    private final ActivationService activationService;

    AuthController(AuthenticationService authenticationService, ActivationService activationService) {
        this.authenticationService = authenticationService;
        this.activationService = activationService;
    }

    @PostMapping("/login")
    ConnexionResponse login(@Valid @RequestBody LoginRequest requete) {
        return versReponse(authenticationService.connecter(requete.identifiant(), requete.motDePasse()));
    }

    @PostMapping("/refresh")
    ConnexionResponse refresh(@Valid @RequestBody RefreshRequest requete) {
        return versReponse(authenticationService.rafraichir(requete.refreshToken()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(@Valid @RequestBody RefreshRequest requete) {
        authenticationService.deconnecter(requete.refreshToken());
    }

    @PostMapping("/activation/{token}/mot-de-passe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void definirMotDePasse(@PathVariable String token, @Valid @RequestBody DefinirMotDePasseRequest requete) {
        if (!requete.motDePasse().equals(requete.confirmation())) {
            throw new MotDePasseInvalideException("Le mot de passe et sa confirmation ne correspondent pas.");
        }
        activationService.definirMotDePasse(token, requete.motDePasse());
    }

    @PostMapping("/activation/{token}/otp")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void validerOtp(@PathVariable String token, @Valid @RequestBody ValiderOtpRequest requete) {
        activationService.validerOtp(token, requete.code());
    }

    @PostMapping("/otp/renvoyer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void renvoyerOtp(@Valid @RequestBody RenvoyerOtpRequest requete) {
        activationService.renvoyerOtp(requete.token());
    }

    private static ConnexionResponse versReponse(ConnexionResultat resultat) {
        return new ConnexionResponse(
                resultat.accessToken(), resultat.refreshToken(), resultat.utilisateurId(),
                resultat.permissions().stream().map(Enum::name).collect(Collectors.toUnmodifiableSet()));
    }
}
