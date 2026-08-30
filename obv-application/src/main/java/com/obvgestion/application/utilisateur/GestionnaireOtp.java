package com.obvgestion.application.utilisateur;

import com.obvgestion.domain.utilisateur.CodeOtp;

/** Port RG-03 — stockage, vérification et limitation des renvois d'OTP. */
public interface GestionnaireOtp {

    void genererEtStocker(Long utilisateurId, CodeOtp code);

    ResultatVerificationOtp verifier(Long utilisateurId, String saisie);

    boolean peutRenvoyer(Long utilisateurId);

    void enregistrerRenvoi(Long utilisateurId);
}
