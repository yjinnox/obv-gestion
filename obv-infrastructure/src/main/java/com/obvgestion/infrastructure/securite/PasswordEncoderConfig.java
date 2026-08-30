package com.obvgestion.infrastructure.securite;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

/**
 * RG-02 — hachage Argon2id, avec repli BCrypt (coût 12) : tout nouveau
 * mot de passe est haché en Argon2id, mais un hachage BCrypt existant
 * reste vérifiable (migration progressive sans invalider les comptes).
 */
@Configuration
public class PasswordEncoderConfig {

    private static final String ID_ARGON2 = "argon2";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new DelegatingPasswordEncoder(ID_ARGON2, Map.of(
                ID_ARGON2, Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8(),
                "bcrypt", new BCryptPasswordEncoder(12)));
    }
}
