package com.obvgestion.infrastructure.securite;

import com.obvgestion.application.utilisateur.EmetteurAccessToken;
import com.obvgestion.domain.utilisateur.Permission;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Émission et vérification du jeton d'accès JWT (§4.4, TTL 15 min). Le
 * jeton porte les permissions effectives calculées à l'authentification,
 * pour éviter un accès base de données à chaque requête autorisée.
 */
@Component
public final class JwtTokenProvider implements EmetteurAccessToken {

    public static final Duration DUREE_ACCESS_TOKEN = Duration.ofMinutes(15);

    private static final String ISSUER = "obv-gestion";
    private static final String CLAIM_PERMISSIONS = "permissions";

    private final SecretKey cle;

    public JwtTokenProvider(@Value("${security.jwt.secret}") String secret) {
        this.cle = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String genererAccessToken(Long utilisateurId, Set<Permission> permissions) {
        return genererAccessToken(utilisateurId, permissions, Instant.now());
    }

    public String genererAccessToken(Long utilisateurId, Set<Permission> permissions, Instant maintenant) {
        List<String> nomsPermissions = permissions.stream().map(Enum::name).collect(Collectors.toList());
        return Jwts.builder()
                .subject(utilisateurId.toString())
                .issuer(ISSUER)
                .issuedAt(Date.from(maintenant))
                .expiration(Date.from(maintenant.plus(DUREE_ACCESS_TOKEN)))
                .claim(CLAIM_PERMISSIONS, nomsPermissions)
                .signWith(cle)
                .compact();
    }

    @SuppressWarnings("unchecked")
    public JwtPrincipal analyser(String token) {
        try {
            Claims claims = Jwts.parser()
                    .requireIssuer(ISSUER)
                    .verifyWith(cle)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long utilisateurId = Long.valueOf(claims.getSubject());
            List<String> nomsPermissions = claims.get(CLAIM_PERMISSIONS, List.class);
            Set<Permission> permissions = nomsPermissions.stream()
                    .map(Permission::valueOf)
                    .collect(Collectors.toUnmodifiableSet());
            return new JwtPrincipal(utilisateurId, permissions);
        } catch (JwtException | IllegalArgumentException e) {
            throw new JetonInvalideException("Jeton d'accès invalide ou expiré.");
        }
    }
}
