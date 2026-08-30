package com.obvgestion.infrastructure.persistence;

import com.obvgestion.application.referentiel.ClientRepository;
import com.obvgestion.domain.referentiel.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class ClientRepositoryAdapter implements ClientRepository {

    private final ClientJpaRepository jpaRepository;

    ClientRepositoryAdapter(ClientJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Client enregistrer(Client client) {
        return jpaRepository.save(client);
    }

    @Override
    public Optional<Client> parId(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Client> parTelephone(String telephone) {
        return jpaRepository.findByTelephone(telephone);
    }

    @Override
    public Page<Client> rechercher(Boolean actif, String recherche, Pageable pageable) {
        return jpaRepository.rechercher(actif, recherche, pageable);
    }
}
