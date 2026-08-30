package com.obvgestion.infrastructure.persistence;

import com.obvgestion.domain.vente.CompteurDocument;
import com.obvgestion.domain.vente.TypeNumeroDocument;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompteurDocumentJpaRepository extends JpaRepository<CompteurDocument, Long> {

    /** RG-26 — verrou pessimiste : bloque les incréments concurrents plutôt que de les rejouer. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c FROM CompteurDocument c
            WHERE c.pointDeVente.id = :pointDeVenteId AND c.type = :type AND c.annee = :annee
            """)
    Optional<CompteurDocument> parCle(@Param("pointDeVenteId") Long pointDeVenteId,
                                       @Param("type") TypeNumeroDocument type, @Param("annee") int annee);
}
