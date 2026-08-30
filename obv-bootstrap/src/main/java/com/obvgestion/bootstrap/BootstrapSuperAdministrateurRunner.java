package com.obvgestion.bootstrap;

import com.obvgestion.application.utilisateur.CreationUtilisateurCommande;
import com.obvgestion.application.utilisateur.UtilisateurRepository;
import com.obvgestion.application.utilisateur.UtilisateurService;
import com.obvgestion.domain.utilisateur.CanalContact;
import com.obvgestion.domain.utilisateur.RoleUtilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RG-06 — il doit toujours rester au moins un SUPER_ADMINISTRATEUR actif.
 * Comme la création d'un utilisateur requiert déjà UTILISATEUR_WRITE (donc
 * un compte existant), le tout premier SUPER_ADMINISTRATEUR est amorcé au
 * démarrage à partir de variables d'environnement, puis suit le parcours
 * d'activation standard (§4.2) : aucun mot de passe n'est jamais codé en dur.
 */
@Component
class BootstrapSuperAdministrateurRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapSuperAdministrateurRunner.class);

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurService utilisateurService;
    private final String nom;
    private final String prenoms;
    private final CanalContact canalContact;
    private final String email;
    private final String telephone;

    BootstrapSuperAdministrateurRunner(UtilisateurRepository utilisateurRepository,
                                        UtilisateurService utilisateurService,
                                        @Value("${bootstrap.super-admin.nom:}") String nom,
                                        @Value("${bootstrap.super-admin.prenoms:}") String prenoms,
                                        @Value("${bootstrap.super-admin.canal-contact:EMAIL}") CanalContact canalContact,
                                        @Value("${bootstrap.super-admin.email:}") String email,
                                        @Value("${bootstrap.super-admin.telephone:}") String telephone) {
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurService = utilisateurService;
        this.nom = nom;
        this.prenoms = prenoms;
        this.canalContact = canalContact;
        this.email = email;
        this.telephone = telephone;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (utilisateurRepository.existeAuMoinsUnAvecRole(RoleUtilisateur.SUPER_ADMINISTRATEUR)) {
            return;
        }
        if (nom.isBlank() || prenoms.isBlank()) {
            log.warn("Aucun SUPER_ADMINISTRATEUR en base et bootstrap.super-admin.* non renseigné : "
                    + "aucun compte ne pourra être créé tant qu'un SUPER_ADMINISTRATEUR n'existe pas.");
            return;
        }

        utilisateurService.creer(new CreationUtilisateurCommande(
                nom, prenoms, canalContact, blankToNull(email), blankToNull(telephone),
                List.of(new CreationUtilisateurCommande.AffectationCommande(
                        RoleUtilisateur.SUPER_ADMINISTRATEUR, null))));
        log.info("Premier SUPER_ADMINISTRATEUR amorcé ({} {}) : invitation d'activation envoyée.", prenoms, nom);
    }

    private static String blankToNull(String valeur) {
        return valeur.isBlank() ? null : valeur;
    }
}
