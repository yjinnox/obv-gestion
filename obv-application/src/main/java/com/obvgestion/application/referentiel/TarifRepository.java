package com.obvgestion.application.referentiel;

import com.obvgestion.domain.referentiel.NatureTarif;
import com.obvgestion.domain.referentiel.Tarif;
import com.obvgestion.domain.referentiel.UniteVente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TarifRepository {

    Tarif enregistrer(Tarif tarif);

    /** RG-08 — le tarif actuellement ouvert (sans date de fin) pour cette clé, s'il existe. */
    Optional<Tarif> tarifOuvert(Long pointDeVenteId, Long produitId, UniteVente uniteVente, NatureTarif nature);

    Page<Tarif> rechercher(Long pointDeVenteId, Long produitId, NatureTarif nature, Pageable pageable);
}
