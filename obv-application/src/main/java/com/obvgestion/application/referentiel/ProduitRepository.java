package com.obvgestion.application.referentiel;

import com.obvgestion.domain.referentiel.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProduitRepository {

    Produit enregistrer(Produit produit);

    Optional<Produit> parId(Long id);

    Page<Produit> rechercher(Long marqueId, Long volumeId, Boolean actif, Pageable pageable);
}
