package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.stock.MouvementStockRepository;
import com.obvgestion.domain.stock.MouvementStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
class MouvementStockRepositoryAdapter implements MouvementStockRepository {

    /**
     * Bornes larges substituées à {@code du}/{@code au} lorsqu'ils sont
     * absents : {@link MouvementStockJpaRepository#rechercher} les compare
     * directement à {@code m.dateHeure} (pas de {@code IS NULL}), pour éviter
     * l'ambiguïté de type PostgreSQL sur un paramètre à la fois testé nul et
     * comparé à une colonne {@code timestamptz}.
     */
    private static final Instant DEBUT_DEFAUT = Instant.parse("0001-01-01T00:00:00Z");
    private static final Instant FIN_DEFAUT = Instant.parse("9999-12-31T23:59:59Z");

    private final MouvementStockJpaRepository jpaRepository;

    MouvementStockRepositoryAdapter(MouvementStockJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MouvementStock enregistrer(MouvementStock mouvement) {
        return jpaRepository.save(mouvement);
    }

    @Override
    public Page<MouvementStock> rechercher(Long pointDeVenteId, Long produitId, Instant du, Instant au,
                                            Pageable pageable) {
        return jpaRepository.rechercher(pointDeVenteId, produitId,
                du == null ? DEBUT_DEFAUT : du, au == null ? FIN_DEFAUT : au, pageable);
    }
}
