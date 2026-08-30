package com.obvgestion.api.utilisateur;

import com.obvgestion.domain.utilisateur.Utilisateur;

import java.util.List;

public record UtilisateurResponse(Long id, String nom, String prenoms, String canalContact, String email,
                                   String telephone, String statut, List<AffectationResponse> affectations) {

    public UtilisateurResponse {
        affectations = List.copyOf(affectations);
    }

    public static UtilisateurResponse de(Utilisateur utilisateur) {
        return new UtilisateurResponse(
                utilisateur.getId(), utilisateur.getNom(), utilisateur.getPrenoms(),
                utilisateur.getCanalContact().name(), utilisateur.getEmail(), utilisateur.getTelephone(),
                utilisateur.getStatut().name(),
                utilisateur.getAffectations().stream()
                        .map(a -> new AffectationResponse(
                                a.getId(), a.getRole().name(),
                                a.getPointDeVente() == null ? null : a.getPointDeVente().getId()))
                        .toList());
    }
}
