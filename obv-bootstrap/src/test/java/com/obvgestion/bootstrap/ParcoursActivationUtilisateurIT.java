package com.obvgestion.bootstrap;

import com.obvgestion.api.auth.ConnexionResponse;
import com.obvgestion.api.auth.DefinirMotDePasseRequest;
import com.obvgestion.api.auth.LoginRequest;
import com.obvgestion.api.auth.ValiderOtpRequest;
import com.obvgestion.api.utilisateur.AffectationRequest;
import com.obvgestion.api.utilisateur.CreerUtilisateurRequest;
import com.obvgestion.api.utilisateur.UtilisateurResponse;
import com.obvgestion.application.utilisateur.CreationUtilisateurCommande;
import com.obvgestion.application.utilisateur.UtilisateurService;
import com.obvgestion.domain.notification.NotificationOutbox;
import com.obvgestion.domain.referentiel.PointDeVente;
import com.obvgestion.domain.referentiel.TypePointDeVente;
import com.obvgestion.domain.utilisateur.CanalContact;
import com.obvgestion.domain.utilisateur.RoleUtilisateur;
import com.obvgestion.infrastructure.persistence.NotificationOutboxRepository;
import com.obvgestion.infrastructure.persistence.PointDeVenteJpaRepository;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vérifie le critère de sortie de P1 (§18) : « Un SUPER_ADMIN peut créer et
 * activer un gérant », de bout en bout contre une vraie base PostgreSQL et
 * un vrai Redis (aucun test sur H2, §17), ainsi que RG-04 (usage unique des
 * jetons et OTP) et RG-03 (épuisement des tentatives OTP).
 */
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "bootstrap.super-admin.nom=Admin",
        "bootstrap.super-admin.prenoms=Test",
        "bootstrap.super-admin.canal-contact=EMAIL",
        "bootstrap.super-admin.email=admin.test@obv-gestion.local"
})
class ParcoursActivationUtilisateurIT {

    private static final Pattern TOKEN_DANS_LIEN = Pattern.compile("token=([\\w-]+)");

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"))
                    .withDatabaseName("obv_gestion")
                    .withUsername("obv")
                    .withPassword("obv");

    @Container
    static final RedisContainer REDIS = new RedisContainer(DockerImageName.parse("redis:7-alpine"));

    @DynamicPropertySource
    static void proprietes(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @LocalServerPort
    private int port;

    @Autowired
    private NotificationOutboxRepository outboxRepository;

    @Autowired
    private PointDeVenteJpaRepository pointDeVenteJpaRepository;

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private ObjectMapper objectMapper;

    private static Long pointDeVenteId;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private <T> HttpResponse<String> poster(String chemin, T corps, String jeton) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl() + chemin))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(corps)));
            if (jeton != null) {
                builder.header("Authorization", "Bearer " + jeton);
            }
            return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponse<String> get(String chemin, String jeton) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(baseUrl() + chemin)).GET();
            if (jeton != null) {
                builder.header("Authorization", "Bearer " + jeton);
            }
            return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String dernierLienActivation(String destinataire) {
        List<NotificationOutbox> entrees = outboxRepository
                .findByDestinataireAndGabaritOrderByCreatedAtDesc(destinataire, "invitation-activation");
        assertThat(entrees).isNotEmpty();
        return valeurVariable(entrees.get(0), "lienActivation");
    }

    private String dernierCodeOtp(String destinataire) {
        List<NotificationOutbox> entrees =
                outboxRepository.findByDestinataireAndGabaritOrderByCreatedAtDesc(destinataire, "otp");
        assertThat(entrees).isNotEmpty();
        return valeurVariable(entrees.get(0), "code");
    }

    @SuppressWarnings("unchecked")
    private String valeurVariable(NotificationOutbox entree, String cle) {
        Map<String, Object> variables = objectMapper.readValue(entree.getVariablesJson(), Map.class);
        return (String) variables.get(cle);
    }

    private String extraireToken(String lienActivation) {
        Matcher matcher = TOKEN_DANS_LIEN.matcher(lienActivation);
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }

    private Long pointDeVenteDepot() {
        if (pointDeVenteId == null) {
            PointDeVente depot = pointDeVenteJpaRepository.save(
                    new PointDeVente("Dépôt central", TypePointDeVente.DEPOT, "Abidjan"));
            pointDeVenteId = depot.getId();
        }
        return pointDeVenteId;
    }

    @Test
    void unSuperAdministrateurPeutCreerEtActiverUnGerant() throws Exception {
        // Le SUPER_ADMINISTRATEUR de bootstrap (RG-06) est amorcé au démarrage.
        String adminEmail = "admin.test@obv-gestion.local";
        String lienActivationAdmin = dernierLienActivation(adminEmail);
        String tokenAdmin = extraireToken(lienActivationAdmin);

        HttpResponse<String> reponseMotDePasse = poster(
                "/auth/activation/" + tokenAdmin + "/mot-de-passe",
                new DefinirMotDePasseRequest("Abcdefgh12", "Abcdefgh12"), null);
        assertThat(reponseMotDePasse.statusCode()).isEqualTo(204);

        String codeOtpAdmin = dernierCodeOtp(adminEmail);
        HttpResponse<String> reponseOtp = poster(
                "/auth/activation/" + tokenAdmin + "/otp", new ValiderOtpRequest(codeOtpAdmin), null);
        assertThat(reponseOtp.statusCode()).isEqualTo(204);

        HttpResponse<String> reponseLogin = poster(
                "/auth/login", new LoginRequest(adminEmail, "Abcdefgh12"), null);
        assertThat(reponseLogin.statusCode()).isEqualTo(200);
        ConnexionResponse connexionAdmin = objectMapper.readValue(reponseLogin.body(), ConnexionResponse.class);
        assertThat(connexionAdmin.permissions()).contains("UTILISATEUR_WRITE", "UTILISATEUR_READ");

        // Le SUPER_ADMIN crée un gérant de dépôt.
        String emailGerant = "gerant.test@obv-gestion.local";
        CreerUtilisateurRequest requeteCreation = new CreerUtilisateurRequest(
                "Kouassi", "Awa", CanalContact.EMAIL, emailGerant, null,
                List.of(new AffectationRequest(RoleUtilisateur.GERANT_DEPOT, pointDeVenteDepot())));
        HttpResponse<String> reponseCreation =
                poster("/utilisateurs", requeteCreation, connexionAdmin.accessToken());
        assertThat(reponseCreation.statusCode()).isEqualTo(201);
        UtilisateurResponse gerantCree = objectMapper.readValue(reponseCreation.body(), UtilisateurResponse.class);
        assertThat(gerantCree.statut()).isEqualTo("EN_ATTENTE_ACTIVATION");

        // Le gérant active son compte par le même parcours (§4.2).
        String tokenGerant = extraireToken(dernierLienActivation(emailGerant));
        assertThat(poster("/auth/activation/" + tokenGerant + "/mot-de-passe",
                new DefinirMotDePasseRequest("Bonjour1234", "Bonjour1234"), null).statusCode()).isEqualTo(204);

        String codeOtpGerant = dernierCodeOtp(emailGerant);
        assertThat(poster("/auth/activation/" + tokenGerant + "/otp",
                new ValiderOtpRequest(codeOtpGerant), null).statusCode()).isEqualTo(204);

        HttpResponse<String> reponseLoginGerant =
                poster("/auth/login", new LoginRequest(emailGerant, "Bonjour1234"), null);
        assertThat(reponseLoginGerant.statusCode()).isEqualTo(200);
        ConnexionResponse connexionGerant =
                objectMapper.readValue(reponseLoginGerant.body(), ConnexionResponse.class);

        // §3.3 — un GERANT_DEPOT ne détient exactement que les permissions de son rôle.
        assertThat(connexionGerant.permissions()).containsExactlyInAnyOrder(
                "REFERENTIEL_READ", "RECEPTION_WRITE", "VENTE_WRITE",
                "SESSION_CLOTURER", "TRANSFERT_WRITE", "RAPPORT_READ");

        // Le gérant n'a pas UTILISATEUR_READ (§3.3) : accès refusé à la liste des comptes.
        HttpResponse<String> listeRefusee = get("/utilisateurs", connexionGerant.accessToken());
        assertThat(listeRefusee.statusCode()).isEqualTo(403);

        // Le SUPER_ADMIN, lui, y accède et retrouve les deux comptes créés.
        HttpResponse<String> listeAutorisee = get("/utilisateurs?size=50", connexionAdmin.accessToken());
        assertThat(listeAutorisee.statusCode()).isEqualTo(200);
        assertThat(listeAutorisee.body()).contains(adminEmail).contains(emailGerant);

        // Accès non authentifié refusé.
        assertThat(get("/utilisateurs", null).statusCode()).isEqualTo(401);

        // RG-06 — un utilisateur ne peut pas s'auto-désactiver ni s'auto-archiver.
        HttpResponse<String> autoArchivage =
                poster("/utilisateurs/1/archiver", Map.of(), connexionAdmin.accessToken());
        assertThat(autoArchivage.statusCode()).isEqualTo(403);
    }

    @Test
    void unJetonDActivationConsommeEstRejeteAvecUnMessageGenerique() throws Exception {
        String email = "jeton-unique.test@obv-gestion.local";
        creerUtilisateurSansRole("Diallo", "Fatou", email);

        String token = extraireToken(dernierLienActivation(email));
        assertThat(poster("/auth/activation/" + token + "/mot-de-passe",
                new DefinirMotDePasseRequest("MotDePasse12", "MotDePasse12"), null).statusCode()).isEqualTo(204);
        String code = dernierCodeOtp(email);
        assertThat(poster("/auth/activation/" + token + "/otp",
                new ValiderOtpRequest(code), null).statusCode()).isEqualTo(204);

        // RG-04 — jeton déjà consommé : message générique, pas de distinction avec un jeton inconnu.
        HttpResponse<String> reutilisation = poster("/auth/activation/" + token + "/mot-de-passe",
                new DefinirMotDePasseRequest("AutreMdp123", "AutreMdp123"), null);
        assertThat(reutilisation.statusCode()).isEqualTo(400);
        assertThat(reutilisation.body()).contains("ACTIVATION_INVALIDE");
    }

    @Test
    void unOtpEpuiseApresCinqEchecsEstInvalide() throws Exception {
        String email = "otp-epuise.test@obv-gestion.local";
        creerUtilisateurSansRole("Traore", "Ibrahim", email);

        String token = extraireToken(dernierLienActivation(email));
        assertThat(poster("/auth/activation/" + token + "/mot-de-passe",
                new DefinirMotDePasseRequest("MotDePasse12", "MotDePasse12"), null).statusCode()).isEqualTo(204);

        // RG-03 — 5 tentatives erronées invalident le code, y compris pour la bonne valeur ensuite.
        for (int i = 0; i < 5; i++) {
            HttpResponse<String> tentative =
                    poster("/auth/activation/" + token + "/otp", new ValiderOtpRequest("000000"), null);
            assertThat(tentative.statusCode()).isEqualTo(400);
        }

        String bonCode = dernierCodeOtp(email);
        HttpResponse<String> apresEpuisement =
                poster("/auth/activation/" + token + "/otp", new ValiderOtpRequest(bonCode), null);
        assertThat(apresEpuisement.statusCode()).isEqualTo(400);
        assertThat(apresEpuisement.body()).contains("ACTIVATION_INVALIDE");
    }

    private void creerUtilisateurSansRole(String nom, String prenoms, String email) {
        utilisateurService.creer(new CreationUtilisateurCommande(
                nom, prenoms, CanalContact.EMAIL, email, null, List.of()));
    }
}
