package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.utilisateur.RoleUtilisateur;
import com.obvgestion.domain.utilisateur.StatutUtilisateur;
import com.obvgestion.domain.utilisateur.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UtilisateurJpaRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    Optional<Utilisateur> findByTelephone(String telephone);

    /**
     * Ne sélectionne que les identifiants : la pagination SQL (LIMIT/OFFSET)
     * n'est fiable qu'en l'absence de fetch join sur une collection
     * ({@code affectations}). Les entités complètes, avec leurs
     * affectations chargées, sont récupérées séparément par
     * {@link #parIdsAvecAffectations}.
     */
    @Query(value = """
            SELECT DISTINCT u.id FROM Utilisateur u
            LEFT JOIN u.affectations a
            WHERE (:statut IS NULL OR u.statut = :statut)
              AND (:role IS NULL OR a.role = :role)
              AND (:pointDeVenteId IS NULL OR a.pointDeVente.id = :pointDeVenteId)
              AND (:recherche IS NULL
                   OR LOWER(u.nom) LIKE LOWER(CONCAT('%', CAST(:recherche AS string), '%'))
                   OR LOWER(u.prenoms) LIKE LOWER(CONCAT('%', CAST(:recherche AS string), '%')))
            """,
            countQuery = """
            SELECT COUNT(DISTINCT u) FROM Utilisateur u
            LEFT JOIN u.affectations a
            WHERE (:statut IS NULL OR u.statut = :statut)
              AND (:role IS NULL OR a.role = :role)
              AND (:pointDeVenteId IS NULL OR a.pointDeVente.id = :pointDeVenteId)
              AND (:recherche IS NULL
                   OR LOWER(u.nom) LIKE LOWER(CONCAT('%', CAST(:recherche AS string), '%'))
                   OR LOWER(u.prenoms) LIKE LOWER(CONCAT('%', CAST(:recherche AS string), '%')))
            """)
    Page<Long> rechercherIds(@Param("statut") StatutUtilisateur statut,
                              @Param("role") RoleUtilisateur role,
                              @Param("pointDeVenteId") Long pointDeVenteId,
                              @Param("recherche") String recherche,
                              Pageable pageable);

    @Query("SELECT DISTINCT u FROM Utilisateur u LEFT JOIN FETCH u.affectations WHERE u.id IN :ids")
    List<Utilisateur> parIdsAvecAffectations(@Param("ids") List<Long> ids);

    @Query("""
            SELECT COUNT(DISTINCT u) FROM Utilisateur u
            JOIN u.affectations a
            WHERE u.statut = :statut AND a.role = :role
            """)
    long compterParStatutEtRole(@Param("statut") StatutUtilisateur statut, @Param("role") RoleUtilisateur role);

    @Query("SELECT COUNT(a) > 0 FROM Affectation a WHERE a.role = :role")
    boolean existeAuMoinsUnAvecRole(@Param("role") RoleUtilisateur role);
}
