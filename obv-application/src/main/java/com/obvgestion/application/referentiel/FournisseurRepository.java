package com.obvgestion.application.referentiel;

import com.obvgestion.domain.referentiel.Fournisseur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface FournisseurRepository {

    Fournisseur enregistrer(Fournisseur fournisseur);

    Optional<Fournisseur> parId(Long id);

    Page<Fournisseur> rechercher(Boolean actif, Pageable pageable);
}
