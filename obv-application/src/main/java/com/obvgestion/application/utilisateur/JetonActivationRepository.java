package com.obvgestion.application.utilisateur;

import com.obvgestion.domain.utilisateur.JetonActivation;

import java.util.Optional;

/** Port de persistance des jetons d'invitation à l'activation (§4.2). */
public interface JetonActivationRepository {

    JetonActivation enregistrer(JetonActivation jeton);

    Optional<JetonActivation> parEmpreinte(String tokenHash);
}
