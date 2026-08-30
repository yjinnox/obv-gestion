package com.obvgestion.application.reception;

import com.obvgestion.domain.reception.JetonValidationReception;

/** Port de persistance des jetons de demande de validation (RG-35/RG-36). */
public interface JetonValidationReceptionRepository {

    JetonValidationReception enregistrer(JetonValidationReception jeton);
}
