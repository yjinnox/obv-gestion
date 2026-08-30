package com.obvgestion.api.securite;

import com.obvgestion.infrastructure.securite.JwtPrincipal;
import com.obvgestion.infrastructure.securite.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/** §4.4 — authentifie la requête à partir du jeton d'accès JWT (en-tête {@code Authorization: Bearer}). */
class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;

    JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String enTete = request.getHeader("Authorization");
        if (enTete != null && enTete.startsWith("Bearer ")) {
            try {
                JwtPrincipal principal = jwtTokenProvider.analyser(enTete.substring("Bearer ".length()));
                List<GrantedAuthority> autorites = principal.permissions().stream()
                        .map(p -> new SimpleGrantedAuthority(p.name()))
                        .map(GrantedAuthority.class::cast)
                        .toList();
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(principal, null, autorites));
            } catch (RuntimeException e) {
                log.debug("Jeton d'accès rejeté : {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
