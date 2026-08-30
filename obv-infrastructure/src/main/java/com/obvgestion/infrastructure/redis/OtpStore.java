package com.obvgestion.infrastructure.redis;

import com.obvgestion.application.utilisateur.GestionnaireOtp;
import com.obvgestion.application.utilisateur.ResultatVerificationOtp;
import com.obvgestion.domain.utilisateur.CodeOtp;
import com.obvgestion.domain.utilisateur.CodeOtpInvalideException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * RG-03 — OTP haché en Redis, TTL 10 minutes, 5 tentatives maximum avant
 * invalidation, 3 renvois maximum par heure.
 */
@Component
public class OtpStore implements GestionnaireOtp {

    public static final Duration TTL_OTP = Duration.ofMinutes(10);
    public static final int TENTATIVES_MAX = 5;
    public static final int RENVOIS_MAX_PAR_HEURE = 3;
    private static final Duration TTL_FENETRE_RENVOI = Duration.ofHours(1);

    private final StringRedisTemplate redis;

    public OtpStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void genererEtStocker(Long utilisateurId, CodeOtp code) {
        redis.opsForValue().set(cleHash(utilisateurId), code.hacher(), TTL_OTP);
        redis.delete(cleTentatives(utilisateurId));
    }

    @Override
    public ResultatVerificationOtp verifier(Long utilisateurId, String saisie) {
        String cleHash = cleHash(utilisateurId);
        String hashAttendu = redis.opsForValue().get(cleHash);
        if (hashAttendu == null) {
            return ResultatVerificationOtp.EXPIRE;
        }

        String hashSaisie = hacherSaisie(saisie);
        if (hashAttendu.equals(hashSaisie)) {
            redis.delete(cleHash);
            redis.delete(cleTentatives(utilisateurId));
            return ResultatVerificationOtp.VALIDE;
        }

        String cleTentatives = cleTentatives(utilisateurId);
        Long tentatives = redis.opsForValue().increment(cleTentatives);
        redis.expire(cleTentatives, TTL_OTP);
        if (tentatives != null && tentatives >= TENTATIVES_MAX) {
            redis.delete(cleHash);
            redis.delete(cleTentatives);
            return ResultatVerificationOtp.EPUISE;
        }
        return ResultatVerificationOtp.INVALIDE;
    }

    private static String hacherSaisie(String saisie) {
        try {
            return new CodeOtp(saisie).hacher();
        } catch (CodeOtpInvalideException e) {
            return "";
        }
    }

    @Override
    public boolean peutRenvoyer(Long utilisateurId) {
        String valeur = redis.opsForValue().get(cleRenvois(utilisateurId));
        int compte = valeur == null ? 0 : Integer.parseInt(valeur);
        return compte < RENVOIS_MAX_PAR_HEURE;
    }

    @Override
    public void enregistrerRenvoi(Long utilisateurId) {
        String cle = cleRenvois(utilisateurId);
        Long compte = redis.opsForValue().increment(cle);
        if (compte != null && compte == 1L) {
            redis.expire(cle, TTL_FENETRE_RENVOI);
        }
    }

    private static String cleHash(Long utilisateurId) {
        return "otp:%d:hash".formatted(utilisateurId);
    }

    private static String cleTentatives(Long utilisateurId) {
        return "otp:%d:tentatives".formatted(utilisateurId);
    }

    private static String cleRenvois(Long utilisateurId) {
        return "otp:%d:renvois".formatted(utilisateurId);
    }
}
