package com.obvgestion.infrastructure.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * §14.3 — compteur Redis à fenêtre fixe, réutilisable pour tout rate
 * limiting applicatif (§16.2, durcissement). Pas de dépendance Servlet
 * ici : le filtre HTTP qui l'utilise vit dans {@code obv-api}, à
 * l'identique du découpage déjà en place pour {@code JwtAuthenticationFilter}.
 */
@Component
public class LimiteurDebitStore {

    private final StringRedisTemplate redis;

    public LimiteurDebitStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /** Incrémente le compteur de {@code cle} (posant sa fenêtre d'expiration au premier appel) et retourne le total. */
    public long incrementer(String cle, Duration fenetre) {
        Long compte = redis.opsForValue().increment(cle);
        if (compte != null && compte == 1L) {
            redis.expire(cle, fenetre);
        }
        return compte == null ? 0L : compte;
    }
}
