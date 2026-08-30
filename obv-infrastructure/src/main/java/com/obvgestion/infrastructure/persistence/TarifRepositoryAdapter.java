package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.referentiel.TarifRepository;
import com.obvgestion.domain.referentiel.NatureTarif;
import com.obvgestion.domain.referentiel.Tarif;
import com.obvgestion.domain.referentiel.UniteVente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class TarifRepositoryAdapter implements TarifRepository {

    private final TarifJpaRepository jpaRepository;

    TarifRepositoryAdapter(TarifJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * Flush immédiat : RG-08 clôt un tarif (UPDATE) puis en crée un nouveau
     * (INSERT) dans la même transaction, or Hibernate ordonne toujours ses
     * inserts avant ses updates au flush — sans ce flush explicite, l'INSERT
     * du nouveau tarif partirait avant l'UPDATE de clôture et violerait
     * l'index unique partiel {@code uk_tarif_ouvert}.
     */
    @Override
    public Tarif enregistrer(Tarif tarif) {
        return jpaRepository.saveAndFlush(tarif);
    }

    @Override
    public Optional<Tarif> tarifOuvert(Long pointDeVenteId, Long produitId, UniteVente uniteVente,
                                        NatureTarif nature) {
        return jpaRepository.tarifOuvert(pointDeVenteId, produitId, uniteVente, nature);
    }

    @Override
    public Page<Tarif> rechercher(Long pointDeVenteId, Long produitId, NatureTarif nature, Pageable pageable) {
        return jpaRepository.rechercher(pointDeVenteId, produitId, nature, pageable);
    }
}
