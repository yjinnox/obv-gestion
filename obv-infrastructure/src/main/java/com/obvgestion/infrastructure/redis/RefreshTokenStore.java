package com.obvgestion.infrastructure.redis;

import com.obvgestion.application.utilisateur.GestionnaireRefreshToken;
import com.obvgestion.domain.commun.Hachage;
import com.obvgestion.domain.commun.JetonOpaque;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * §4.4 — refresh token rotatif (TTL 7 jours), stocké dans Redis et
 * révocable. Chaque utilisation consomme le jeton (rotation) : un nouveau
 * jeton est réémis à chaque rafraîchissement, l'ancien devient invalide.
 */
@Component
public class RefreshTokenStore implements GestionnaireRefreshToken {

    public static final Duration TTL_REFRESH_TOKEN = Duration.ofDays(7);

    private final StringRedisTemplate redis;

    public RefreshTokenStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /** Émet un nouveau jeton et retourne sa valeur en clair (à ne transmettre qu'une fois). */
    @Override
    public String emettre(Long utilisateurId) {
        JetonOpaque jeton = JetonOpaque.genererAleatoire();
        redis.opsForValue().set(cle(jeton.hacher()), utilisateurId.toString(), TTL_REFRESH_TOKEN);
        return jeton.valeurClaire();
    }

    /** Consomme le jeton fourni (rotation) et retourne l'utilisateur associé, s'il est valide. */
    @Override
    public Optional<Long> consommer(String tokenClair) {
        String cle = cle(Hachage.sha256Hex(tokenClair));
        String utilisateurId = redis.opsForValue().get(cle);
        if (utilisateurId == null) {
            return Optional.empty();
        }
        redis.delete(cle);
        return Optional.of(Long.valueOf(utilisateurId));
    }

    @Override
    public void revoquer(String tokenClair) {
        redis.delete(cle(Hachage.sha256Hex(tokenClair)));
    }

    private static String cle(String tokenHash) {
        return "refresh:%s".formatted(tokenHash);
    }
}
