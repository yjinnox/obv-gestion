package com.obvgestion.infrastructure.redis;

import com.obvgestion.application.vente.PanierRepository;
import com.obvgestion.domain.vente.Panier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

/** §8.2 — panier persisté en Redis (TTL 4h, clé = utilisateur + session), jamais en base (RG-25). */
@Component
public class PanierRepositoryAdapter implements PanierRepository {

    public static final Duration TTL_PANIER = Duration.ofHours(4);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public PanierRepositoryAdapter(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    @Override
    public Panier trouver(Long utilisateurId, Long sessionVenteId) {
        String json = redis.opsForValue().get(cle(utilisateurId, sessionVenteId));
        if (json == null) {
            return Panier.vide(utilisateurId, sessionVenteId);
        }
        return objectMapper.readValue(json, Panier.class);
    }

    @Override
    public void enregistrer(Panier panier) {
        String json = objectMapper.writeValueAsString(panier);
        redis.opsForValue().set(cle(panier.utilisateurId(), panier.sessionVenteId()), json, TTL_PANIER);
    }

    @Override
    public void supprimer(Long utilisateurId, Long sessionVenteId) {
        redis.delete(cle(utilisateurId, sessionVenteId));
    }

    private static String cle(Long utilisateurId, Long sessionVenteId) {
        return "panier:%d:%d".formatted(utilisateurId, sessionVenteId);
    }
}
