package com.obvgestion.api.vente;

import com.obvgestion.api.referentiel.CreerClientRequest;
import com.obvgestion.domain.vente.ModePaiement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * §8.2 étape 6 — « Commander » à partir du panier courant. {@code clientId}
 * ou {@code nouveauClient} (mutuellement exclusifs) : client existant
 * sélectionné, ou créé à la volée. L'idempotence (RG-27) est portée par
 * l'en-tête HTTP {@code Idempotency-Key}, pas par le corps de la requête.
 */
public record CreerCommandeRequest(@NotNull Long sessionVenteId, Long clientId,
                                    @Valid CreerClientRequest nouveauClient, @NotNull ModePaiement modePaiement) {
}
