package com.obvgestion.application.utilisateur;

/** Port §4.4 — verrouillage temporaire après 5 échecs de connexion (15 minutes). */
public interface LimiteurConnexion {

    boolean estVerrouille(String identifiant);

    void enregistrerEchec(String identifiant);

    void reinitialiser(String identifiant);
}
