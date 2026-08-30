package com.obvgestion.application.utilisateur;

import com.obvgestion.domain.utilisateur.RoleUtilisateur;
import com.obvgestion.domain.utilisateur.StatutUtilisateur;
import com.obvgestion.domain.utilisateur.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/** Port de persistance des comptes utilisateurs, implémenté en infrastructure. */
public interface UtilisateurRepository {

    Utilisateur enregistrer(Utilisateur utilisateur);

    Optional<Utilisateur> parId(Long id);

    Optional<Utilisateur> parEmail(String email);

    Optional<Utilisateur> parTelephone(String telephone);

    Page<Utilisateur> rechercher(StatutUtilisateur statut, RoleUtilisateur role, Long pointDeVenteId,
                                  String recherche, Pageable pageable);

    long compterParStatutEtRole(StatutUtilisateur statut, RoleUtilisateur role);

    boolean existeAuMoinsUnAvecRole(RoleUtilisateur role);
}
