package com.obvgestion.domain.commun;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Empreinte SHA-256 pour les valeurs à haute entropie (jetons, OTP) avant persistance. */
public final class Hachage {

    private Hachage() {
    }

    public static String sha256Hex(String valeur) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] empreinte = digest.digest(valeur.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(empreinte);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
