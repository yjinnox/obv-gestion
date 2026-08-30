package com.obvgestion.application.audit;

import com.obvgestion.domain.audit.TypeActionJournal;

/**
 * Port de journalisation des actions sensibles (§12) : validation,
 * annulation, modification post-clôture, changement de droits,
 * activation/désactivation de compte.
 */
public interface Journalisateur {

    /**
     * @param acteur      identifiant (email/téléphone) de l'auteur de l'action
     * @param cibleType   type de l'entité affectée (ex. {@code "Utilisateur"})
     * @param cibleId     identifiant de l'entité affectée
     * @param valeursAvant état avant l'action, sérialisé en JSON (nullable)
     * @param valeursApres état après l'action, sérialisé en JSON (nullable)
     * @param adresseIp   adresse IP de l'appelant (nullable)
     */
    void journaliser(String acteur, TypeActionJournal action, String cibleType, String cibleId,
                      String valeursAvant, String valeursApres, String adresseIp);
}
