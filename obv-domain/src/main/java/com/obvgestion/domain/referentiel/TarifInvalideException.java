package com.obvgestion.domain.referentiel;

import com.obvgestion.domain.commun.RegleGestionException;

/** RG-08/RG-10 — violation d'une règle de tarification. */
public final class TarifInvalideException extends RegleGestionException {
    public TarifInvalideException(String message) {
        super("TARIF_INVALIDE", message);
    }
}
