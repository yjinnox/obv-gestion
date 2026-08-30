package com.obvgestion.domain.commun;

import java.util.UUID;

/**
 * Jeton opaque à haute entropie (invitation d'activation, lien de
 * validation…) : seule son empreinte SHA-256 est destinée à être persistée
 * (§4.2, RG-35), la valeur en clair n'étant transmise qu'une fois au
 * destinataire via le lien envoyé par SMS/email.
 */
public record JetonOpaque(String valeurClaire) {

    public static JetonOpaque genererAleatoire() {
        return new JetonOpaque(UUID.randomUUID().toString());
    }

    public String hacher() {
        return Hachage.sha256Hex(valeurClaire);
    }
}
