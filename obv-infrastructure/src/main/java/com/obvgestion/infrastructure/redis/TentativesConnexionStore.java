package com.obvgestion.infrastructure.redis;

import com.obvgestion.application.utilisateur.LimiteurConnexion;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** §4.4 — verrouillage temporaire du compte après 5 échecs de connexion (15 minutes). */
@Component
public class TentativesConnexionStore implements LimiteurConnexion {

    public static final int ECHECS_MAX = 5;
    public static final Duration DUREE_VERROUILLAGE = Duration.ofMinutes(15);

    private final StringRedisTemplate redis;

    public TentativesConnexionStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public boolean estVerrouille(String identifiant) {
        String valeur = redis.opsForValue().get(cle(identifiant));
        return valeur != null && Integer.parseInt(valeur) >= ECHECS_MAX;
    }

    @Override
    public void enregistrerEchec(String identifiant) {
        String cle = cle(identifiant);
        Long compte = redis.opsForValue().increment(cle);
        if (compte != null && compte == 1L) {
            redis.expire(cle, DUREE_VERROUILLAGE);
        }
    }

    @Override
    public void reinitialiser(String identifiant) {
        redis.delete(cle(identifiant));
    }

    private static String cle(String identifiant) {
        return "login:echecs:%s".formatted(identifiant.toLowerCase());
    }
}
