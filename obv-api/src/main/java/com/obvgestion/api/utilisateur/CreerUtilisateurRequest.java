package com.obvgestion.api.utilisateur;

import com.obvgestion.domain.utilisateur.CanalContact;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreerUtilisateurRequest(String nom, String prenoms, @NotNull CanalContact canalContact,
                                       String email, String telephone,
                                       @NotEmpty @Valid List<AffectationRequest> affectations) {

    public CreerUtilisateurRequest {
        // Copie défensive nulle-sûre : un affectations manquant doit rester
        // null pour que @NotEmpty produise son message de validation habituel.
        affectations = affectations == null ? null : List.copyOf(affectations);
    }
}
