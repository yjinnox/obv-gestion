package com.obvgestion.infrastructure.securite;

import com.obvgestion.application.utilisateur.HacheurMotDePasse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class HacheurMotDePasseAdapter implements HacheurMotDePasse {

    private final PasswordEncoder passwordEncoder;

    HacheurMotDePasseAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hacher(String motDePasseClair) {
        return passwordEncoder.encode(motDePasseClair);
    }

    @Override
    public boolean verifier(String motDePasseClair, String hash) {
        return passwordEncoder.matches(motDePasseClair, hash);
    }
}
