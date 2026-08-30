package com.obvgestion.api.referentiel;

import com.obvgestion.domain.referentiel.TypeClient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** RG-07 — les champs conditionnés par le type sont validés dans le domaine ({@code Client.creer}). */
public record CreerClientRequest(@NotNull TypeClient type, String nom, String prenoms, String raisonSociale,
                                  @NotBlank String telephone, String email, String adresseFacturation) {
}
