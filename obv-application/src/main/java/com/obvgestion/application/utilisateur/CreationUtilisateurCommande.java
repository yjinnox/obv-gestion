package com.obvgestion.application.utilisateur;

import com.obvgestion.domain.utilisateur.CanalContact;
import com.obvgestion.domain.utilisateur.RoleUtilisateur;

import java.util.List;

/** §4.1 — saisie de création d'un compte utilisateur. */
public record CreationUtilisateurCommande(String nom, String prenoms, CanalContact canalContact, String email,
                                           String telephone, List<AffectationCommande> affectations) {

    public CreationUtilisateurCommande {
        affectations = List.copyOf(affectations);
    }

    public record AffectationCommande(RoleUtilisateur role, Long pointDeVenteId) {
    }
}
