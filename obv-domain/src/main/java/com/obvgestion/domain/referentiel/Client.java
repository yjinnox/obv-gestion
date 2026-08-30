package com.obvgestion.domain.referentiel;

import com.obvgestion.domain.audit.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Client du dépôt (§5.1), sélectionné ou créé à la volée lors d'une commande (§8.2). */
@Entity
@Table(name = "client")
public class Client extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeClient type;

    private String nom;

    private String prenoms;

    @Column(name = "raison_sociale")
    private String raisonSociale;

    @Column(nullable = false)
    private String telephone;

    private String email;

    @Column(name = "adresse_facturation")
    private String adresseFacturation;

    @Column(nullable = false)
    private boolean actif = true;

    protected Client() {
    }

    private Client(TypeClient type, String nom, String prenoms, String raisonSociale, String telephone,
                    String email, String adresseFacturation) {
        this.type = type;
        this.nom = nom;
        this.prenoms = prenoms;
        this.raisonSociale = raisonSociale;
        this.telephone = telephone;
        this.email = email;
        this.adresseFacturation = adresseFacturation;
    }

    /**
     * RG-07 — {@code raisonSociale} obligatoire si {@code ENTREPRISE},
     * interdite sinon ; {@code nom}/{@code prenoms} obligatoires si
     * {@code PARTICULIER}.
     */
    public static Client creer(TypeClient type, String nom, String prenoms, String raisonSociale, String telephone,
                                String email, String adresseFacturation) {
        if (telephone == null || telephone.isBlank()) {
            throw new ClientInvalideException("Le téléphone est obligatoire.");
        }
        if (type == TypeClient.ENTREPRISE) {
            if (raisonSociale == null || raisonSociale.isBlank()) {
                throw new ClientInvalideException("La raison sociale est obligatoire pour un client ENTREPRISE.");
            }
        } else {
            if (raisonSociale != null && !raisonSociale.isBlank()) {
                throw new ClientInvalideException("La raison sociale est interdite pour un client PARTICULIER.");
            }
            if (nom == null || nom.isBlank() || prenoms == null || prenoms.isBlank()) {
                throw new ClientInvalideException("Le nom et les prénoms sont obligatoires pour un client PARTICULIER.");
            }
        }
        return new Client(type, nom, prenoms, raisonSociale, telephone, email, adresseFacturation);
    }

    public Long getId() {
        return id;
    }

    public TypeClient getType() {
        return type;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenoms() {
        return prenoms;
    }

    public String getRaisonSociale() {
        return raisonSociale;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdresseFacturation() {
        return adresseFacturation;
    }

    public void setAdresseFacturation(String adresseFacturation) {
        this.adresseFacturation = adresseFacturation;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
