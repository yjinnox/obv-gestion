package com.obvgestion.application.utilisateur;

/** Port de hachage / vérification du mot de passe (RG-02). */
public interface HacheurMotDePasse {

    String hacher(String motDePasseClair);

    boolean verifier(String motDePasseClair, String hash);
}
