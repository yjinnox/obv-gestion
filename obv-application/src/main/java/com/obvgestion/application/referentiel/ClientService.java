package com.obvgestion.application.referentiel;

import com.obvgestion.domain.referentiel.Client;
import com.obvgestion.domain.referentiel.ClientInvalideException;
import com.obvgestion.domain.referentiel.TypeClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class ClientService {

    private final ClientRepository repository;

    public ClientService(ClientRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Client creer(TypeClient type, String nom, String prenoms, String raisonSociale, String telephone,
                         String email, String adresseFacturation) {
        if (repository.parTelephone(telephone).isPresent()) {
            throw new ClientInvalideException("Ce numéro de téléphone est déjà utilisé par un autre client.");
        }
        return repository.enregistrer(
                Client.creer(type, nom, prenoms, raisonSociale, telephone, email, adresseFacturation));
    }

    @Transactional
    public Client modifier(Long id, String telephone, String email, String adresseFacturation, boolean actif) {
        Client client = trouver(id);
        client.setTelephone(telephone);
        client.setEmail(email);
        client.setAdresseFacturation(adresseFacturation);
        client.setActif(actif);
        return repository.enregistrer(client);
    }

    @Transactional
    public void desactiver(Long id) {
        Client client = trouver(id);
        client.setActif(false);
        repository.enregistrer(client);
    }

    @Transactional(readOnly = true)
    public Client trouver(Long id) {
        return repository.parId(id).orElseThrow(() -> new NoSuchElementException("Client introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public Page<Client> rechercher(Boolean actif, String recherche, Pageable pageable) {
        return repository.rechercher(actif, recherche, pageable);
    }
}
