package com.obvgestion.domain.utilisateur;

import com.obvgestion.domain.audit.Auditable;
import com.obvgestion.domain.notification.CanalNotification;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Compte utilisateur (§4). La création, l'activation et l'administration
 * d'un compte sont détaillées en §4.1 à §4.3.
 */
@Entity
@Table(name = "utilisateur")
@Audited
public class Utilisateur extends Auditable {

    private static final Pattern TELEPHONE_E164 = Pattern.compile("^\\+[1-9]\\d{6,14}$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenoms;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_contact", nullable = false, length = 20)
    private CanalContact canalContact;

    private String email;

    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutUtilisateur statut;

    @Column(name = "mot_de_passe_hash")
    private String motDePasseHash;

    @Column(name = "date_archivage")
    private Instant dateArchivage;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotAudited
    private List<Affectation> affectations = new ArrayList<>();

    protected Utilisateur() {
    }

    private Utilisateur(String nom, String prenoms, CanalContact canalContact, String email, String telephone) {
        this.nom = nom;
        this.prenoms = prenoms;
        this.canalContact = canalContact;
        this.email = email;
        this.telephone = telephone;
        this.statut = StatutUtilisateur.EN_ATTENTE_ACTIVATION;
    }

    /**
     * Crée un compte au statut {@code EN_ATTENTE_ACTIVATION} (§4.1). Le
     * canal de contact détermine quel identifiant est obligatoire : email
     * valide si {@code EMAIL}, téléphone au format E.164 si
     * {@code TELEPHONE}.
     */
    public static Utilisateur creer(String nom, String prenoms, CanalContact canalContact,
                                     String email, String telephone) {
        if (nom == null || nom.isBlank() || prenoms == null || prenoms.isBlank()) {
            throw new IllegalArgumentException("Le nom et les prénoms sont obligatoires.");
        }
        if (canalContact == CanalContact.EMAIL) {
            if (email == null || !email.contains("@")) {
                throw new IllegalArgumentException("Une adresse email valide est requise pour le canal EMAIL.");
            }
        } else {
            if (telephone == null || !TELEPHONE_E164.matcher(telephone).matches()) {
                throw new IllegalArgumentException(
                        "Un numéro de téléphone au format E.164 est requis pour le canal TELEPHONE.");
            }
        }
        return new Utilisateur(nom, prenoms, canalContact, email, telephone);
    }

    /**
     * RG-02/RG-04 — appelée à l'issue de la saisie du mot de passe lors de
     * l'activation ; le compte reste {@code EN_ATTENTE_ACTIVATION} tant que
     * l'OTP n'a pas été validé (cf. {@link #activer()}).
     */
    public void definirMotDePasse(String motDePasseHash) {
        exigerStatut(StatutUtilisateur.EN_ATTENTE_ACTIVATION, "définir le mot de passe");
        this.motDePasseHash = motDePasseHash;
    }

    /** Bascule le compte à {@code ACTIF} une fois l'OTP validé (§4.2). */
    public void activer() {
        exigerStatut(StatutUtilisateur.EN_ATTENTE_ACTIVATION, "activer le compte");
        if (motDePasseHash == null) {
            throw new EtatUtilisateurInvalideException(
                    "Le mot de passe doit être défini avant l'activation du compte.");
        }
        this.statut = StatutUtilisateur.ACTIF;
    }

    /** Réactive un compte désactivé (§4.3), sans passer par une nouvelle activation. */
    public void reactiver() {
        exigerStatut(StatutUtilisateur.DESACTIVE, "réactiver le compte");
        this.statut = StatutUtilisateur.ACTIF;
    }

    /** §4.3 — désactivation réversible, distincte de l'archivage (RG-05). */
    public void desactiver() {
        if (statut != StatutUtilisateur.ACTIF) {
            throw new EtatUtilisateurInvalideException("Seul un compte ACTIF peut être désactivé.");
        }
        this.statut = StatutUtilisateur.DESACTIVE;
    }

    /**
     * RG-05 — archivage définitif (aucun {@code DELETE} physique) : un
     * compte archivé ne peut plus se connecter ni apparaître dans les
     * listes de sélection.
     */
    public void archiver(Instant maintenant) {
        if (statut == StatutUtilisateur.ARCHIVE) {
            throw new EtatUtilisateurInvalideException("Le compte est déjà archivé.");
        }
        this.statut = StatutUtilisateur.ARCHIVE;
        this.dateArchivage = maintenant;
    }

    /**
     * §4.3 — réinitialisation du mot de passe par un administrateur : fait
     * repasser le compte par le parcours d'activation (§4.2) avec un
     * nouveau jeton d'invitation.
     */
    public void reinitialiserPourNouvelleActivation() {
        if (statut == StatutUtilisateur.ARCHIVE) {
            throw new EtatUtilisateurInvalideException("Un compte archivé ne peut pas être réinitialisé.");
        }
        this.statut = StatutUtilisateur.EN_ATTENTE_ACTIVATION;
        this.motDePasseHash = null;
    }

    private void exigerStatut(StatutUtilisateur attendu, String action) {
        if (statut != attendu) {
            throw new EtatUtilisateurInvalideException(
                    "Impossible de " + action + " : statut actuel " + statut + " (attendu " + attendu + ").");
        }
    }

    public void renommer(String nom, String prenoms) {
        if (nom == null || nom.isBlank() || prenoms == null || prenoms.isBlank()) {
            throw new IllegalArgumentException("Le nom et les prénoms sont obligatoires.");
        }
        this.nom = nom;
        this.prenoms = prenoms;
    }

    public boolean possedeRole(RoleUtilisateur role) {
        return affectations.stream().anyMatch(a -> a.getRole() == role);
    }

    public Set<Permission> permissions() {
        return affectations.stream()
                .flatMap(a -> a.getRole().permissions().stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenoms() {
        return prenoms;
    }

    public CanalContact getCanalContact() {
        return canalContact;
    }

    public String getEmail() {
        return email;
    }

    public String getTelephone() {
        return telephone;
    }

    /** §11 — canal de notification associé au canal de contact préféré de l'utilisateur. */
    public CanalNotification canalNotification() {
        return canalContact == CanalContact.EMAIL ? CanalNotification.EMAIL : CanalNotification.SMS;
    }

    /** §11 — adresse ou numéro à notifier, cohérent avec {@link #canalNotification()}. */
    public String contactNotification() {
        return canalContact == CanalContact.EMAIL ? email : telephone;
    }

    public StatutUtilisateur getStatut() {
        return statut;
    }

    public String getMotDePasseHash() {
        return motDePasseHash;
    }

    public Instant getDateArchivage() {
        return dateArchivage;
    }

    public List<Affectation> getAffectations() {
        return List.copyOf(affectations);
    }

    /** Utilisé par le service applicatif après validation de {@link Affectation#of}. */
    public void ajouterAffectation(Affectation affectation) {
        affectations.add(affectation);
    }

    public void retirerAffectation(Affectation affectation) {
        affectations.remove(affectation);
    }
}
