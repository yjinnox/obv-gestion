package com.obvgestion.api.securite;

import com.obvgestion.infrastructure.redis.LimiteurDebitStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;

/**
 * §14.3 (rate limiting via Redis) / §16.2 (durcissement) — limite le débit
 * des routes d'authentification non protégées par jeton
 * ({@code /api/v1/auth/**}), par adresse IP cliente. Complète, sans le
 * remplacer, le verrouillage de compte ({@code TentativesConnexionStore},
 * 5 échecs sur un même identifiant) : ce filtre protège aussi contre le
 * bourrage réparti sur plusieurs comptes et l'abus de
 * {@code /otp/renvoyer}, qui n'a autrement aucune limite propre.
 * L'adresse IP cliente suppose {@code server.forward-headers-strategy=framework}
 * derrière un reverse proxy de confiance (nginx, Ingress).
 */
@Component
class AuthRateLimitFilter extends OncePerRequestFilter {

    static final int LIMITE_REQUETES = 30;
    static final Duration FENETRE = Duration.ofMinutes(1);

    private static final String PREFIXE_ROUTE = "/api/v1/auth";

    private final LimiteurDebitStore limiteur;
    private final ObjectMapper objectMapper;

    AuthRateLimitFilter(LimiteurDebitStore limiteur, ObjectMapper objectMapper) {
        this.limiteur = limiteur;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!request.getRequestURI().startsWith(PREFIXE_ROUTE)) {
            chain.doFilter(request, response);
            return;
        }

        long compte = limiteur.incrementer("ratelimit:auth:%s".formatted(request.getRemoteAddr()), FENETRE);
        if (compte > LIMITE_REQUETES) {
            ecrireTropDeRequetes(response);
            return;
        }
        chain.doFilter(request, response);
    }

    private void ecrireTropDeRequetes(HttpServletResponse response) throws IOException {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS,
                "Trop de requêtes : veuillez réessayer plus tard.");
        problemDetail.setProperty("code", "TROP_DE_REQUETES");

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(problemDetail));
    }
}
