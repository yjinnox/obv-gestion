package com.obvgestion.application.referentiel;

import com.obvgestion.domain.commun.Montant;
import com.obvgestion.domain.referentiel.NatureTarif;
import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.referentiel.Produit;
import com.obvgestion.domain.referentiel.Tarif;
import com.obvgestion.domain.referentiel.UniteVente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.NoSuchElementException;

/** §5.2 — tarification datée. */
@Service
public class TarifService {

    private final TarifRepository repository;
    private final PointDeVenteRepository pointDeVenteRepository;
    private final ProduitRepository produitRepository;

    public TarifService(TarifRepository repository, PointDeVenteRepository pointDeVenteRepository,
                         ProduitRepository produitRepository) {
        this.repository = repository;
        this.pointDeVenteRepository = pointDeVenteRepository;
        this.produitRepository = produitRepository;
    }

    /**
     * RG-08 — clôt le tarif actuellement ouvert pour cette clé (le cas
     * échéant) avant d'en créer un nouveau : un seul tarif actif par
     * (point de vente, produit, unité de vente, nature) à un instant T.
     */
    @Transactional
    public Tarif creer(Long pointDeVenteId, Long produitId, UniteVente uniteVente, NatureTarif nature,
                        Montant montant, LocalDate dateDebut) {
        PointDeVente pointDeVente = pointDeVenteRepository.parId(pointDeVenteId)
                .orElseThrow(() -> new NoSuchElementException("Point de vente introuvable : " + pointDeVenteId));
        Produit produit = produitRepository.parId(produitId)
                .orElseThrow(() -> new NoSuchElementException("Produit introuvable : " + produitId));

        repository.tarifOuvert(pointDeVenteId, produitId, uniteVente, nature).ifPresent(tarifCourant -> {
            tarifCourant.cloturer(dateDebut);
            repository.enregistrer(tarifCourant);
        });

        return repository.enregistrer(Tarif.creer(pointDeVente, produit, uniteVente, nature, montant, dateDebut));
    }

    @Transactional(readOnly = true)
    public Page<Tarif> rechercher(Long pointDeVenteId, Long produitId, NatureTarif nature, Pageable pageable) {
        return repository.rechercher(pointDeVenteId, produitId, nature, pageable);
    }
}
