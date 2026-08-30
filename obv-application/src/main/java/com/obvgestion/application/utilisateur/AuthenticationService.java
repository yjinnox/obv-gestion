package com.obvgestion.application.utilisateur;

import com.obvgestion.domain.utilisateur.CompteVerrouilleException;
import com.obvgestion.domain.utilisateur.IdentifiantsInvalidesException;
import com.obvgestion.domain.utilisateur.StatutUtilisateur;
import com.obvgestion.domain.utilisateur.Utilisateur;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** §4.4 — authentification, rafraîchissement et déconnexion. */
@Service
public class AuthenticationService {

    private final UtilisateurRepository utilisateurRepository;
    private final HacheurMotDePasse hacheurMotDePasse;
    private final EmetteurAccessToken emetteurAccessToken;
    private final GestionnaireRefreshToken gestionnaireRefreshToken;
    private final LimiteurConnexion limiteurConnexion;

    public AuthenticationService(UtilisateurRepository utilisateurRepository,
                                  HacheurMotDePasse hacheurMotDePasse,
                                  EmetteurAccessToken emetteurAccessToken,
                                  GestionnaireRefreshToken gestionnaireRefreshToken,
                                  LimiteurConnexion limiteurConnexion) {
        this.utilisateurRepository = utilisateurRepository;
        this.hacheurMotDePasse = hacheurMotDePasse;
        this.emetteurAccessToken = emetteurAccessToken;
        this.gestionnaireRefreshToken = gestionnaireRefreshToken;
        this.limiteurConnexion = limiteurConnexion;
    }

    @Transactional
    public ConnexionResultat connecter(String identifiant, String motDePasseClair) {
        if (limiteurConnexion.estVerrouille(identifiant)) {
            throw new CompteVerrouilleException();
        }

        Optional<Utilisateur> utilisateurTrouve = utilisateurRepository.parEmail(identifiant)
                .or(() -> utilisateurRepository.parTelephone(identifiant));

        boolean identifiantsValides = utilisateurTrouve.isPresent()
                && utilisateurTrouve.get().getStatut() == StatutUtilisateur.ACTIF
                && hacheurMotDePasse.verifier(motDePasseClair, utilisateurTrouve.get().getMotDePasseHash());

        if (!identifiantsValides) {
            limiteurConnexion.enregistrerEchec(identifiant);
            throw new IdentifiantsInvalidesException();
        }

        limiteurConnexion.reinitialiser(identifiant);
        return emettreResultat(utilisateurTrouve.get());
    }

    @Transactional
    public ConnexionResultat rafraichir(String refreshTokenClair) {
        Long utilisateurId = gestionnaireRefreshToken.consommer(refreshTokenClair)
                .orElseThrow(IdentifiantsInvalidesException::new);
        Utilisateur utilisateur = utilisateurRepository.parId(utilisateurId)
                .filter(u -> u.getStatut() == StatutUtilisateur.ACTIF)
                .orElseThrow(IdentifiantsInvalidesException::new);
        return emettreResultat(utilisateur);
    }

    public void deconnecter(String refreshTokenClair) {
        gestionnaireRefreshToken.revoquer(refreshTokenClair);
    }

    private ConnexionResultat emettreResultat(Utilisateur utilisateur) {
        var permissions = utilisateur.permissions();
        String accessToken = emetteurAccessToken.genererAccessToken(utilisateur.getId(), permissions);
        String refreshToken = gestionnaireRefreshToken.emettre(utilisateur.getId());
        return new ConnexionResultat(accessToken, refreshToken, utilisateur.getId(), permissions);
    }
}
