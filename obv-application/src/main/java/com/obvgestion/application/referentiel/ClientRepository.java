package com.obvgestion.application.referentiel;

import com.obvgestion.domain.referentiel.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ClientRepository {

    Client enregistrer(Client client);

    Optional<Client> parId(Long id);

    Optional<Client> parTelephone(String telephone);

    Page<Client> rechercher(Boolean actif, String recherche, Pageable pageable);
}
