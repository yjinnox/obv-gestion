package com.obvgestion.application.vente;

import com.obvgestion.domain.vente.Panier;

/** RG-24 — contrôle de disponibilité informatif (non bloquant) à l'ajout au panier. */
public record ResultatAjoutPanier(Panier panier, long stockDisponibleDemiCasiers) {
}
