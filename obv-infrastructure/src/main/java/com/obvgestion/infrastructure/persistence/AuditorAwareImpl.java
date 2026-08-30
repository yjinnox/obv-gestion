package com.obvgestion.infrastructure.persistence;

import com.obvgestion.infrastructure.securite.JwtPrincipal;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** §12 — auteur courant pour {@code createdBy}/{@code updatedBy} (JPA Auditing). */
@Component("auditorAware")
class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtPrincipal principal)) {
            return Optional.of("system");
        }
        return Optional.of(principal.utilisateurId().toString());
    }
}
