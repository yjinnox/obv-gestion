package com.obvgestion.domain.utilisateur;

/**
 * Canal de contact déclaré à la création d'un utilisateur (§4.1) : détermine
 * lequel de l'email ou du téléphone est obligatoire, et le canal utilisé
 * pour l'invitation d'activation et l'OTP (§4.2).
 */
public enum CanalContact {
    EMAIL,
    TELEPHONE
}
