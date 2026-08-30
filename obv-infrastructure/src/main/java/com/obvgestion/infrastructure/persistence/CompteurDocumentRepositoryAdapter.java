package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.vente.CompteurDocumentRepository;
import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.vente.CompteurDocument;
import com.obvgestion.domain.vente.TypeNumeroDocument;
import org.springframework.stereotype.Repository;

@Repository
class CompteurDocumentRepositoryAdapter implements CompteurDocumentRepository {

    private final CompteurDocumentJpaRepository jpaRepository;

    CompteurDocumentRepositoryAdapter(CompteurDocumentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public String prochainNumero(PointDeVente pointDeVente, TypeNumeroDocument type, int annee) {
        CompteurDocument compteur = jpaRepository.parCle(pointDeVente.getId(), type, annee)
                .orElseGet(() -> jpaRepository.save(new CompteurDocument(pointDeVente, type, annee)));
        long numero = compteur.incrementerEtObtenir();
        jpaRepository.save(compteur);
        return compteur.formaterNumero(pointDeVente.getId(), numero);
    }
}
