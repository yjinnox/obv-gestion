package com.obvgestion.api.utilisateur;

import com.obvgestion.domain.utilisateur.RoleUtilisateur;
import jakarta.validation.constraints.NotNull;

public record AffectationRequest(@NotNull RoleUtilisateur role, Long pointDeVenteId) {
}
