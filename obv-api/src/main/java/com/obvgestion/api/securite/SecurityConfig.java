package com.obvgestion.api.securite;

import com.obvgestion.infrastructure.securite.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

/**
 * §4.4, §13 — API sans état, authentifiée par jeton JWT. CSRF désactivé
 * (aucune session de navigateur, uniquement des jetons Bearer).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {

    /**
     * Actuator (santé, métriques) vit sur {@code management.server.port},
     * un contexte Web séparé hors de cette chaîne de filtres : il n'y a
     * donc plus de route Actuator à exempter ici (§16.2, durcissement).
     */
    private static final String[] ROUTES_PUBLIQUES = {
            "/api/v1/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private final ObjectMapper objectMapper;
    private final List<String> originesAutorisees;

    SecurityConfig(ObjectMapper objectMapper,
                    @Value("${app.cors-allowed-origins}") String originesCorsAutorisees) {
        this.objectMapper = objectMapper;
        this.originesAutorisees = List.of(originesCorsAutorisees.split("\\s*,\\s*"));
    }

    /**
     * §16.2 (durcissement) — un {@code SecurityFilterChain} personnalisé
     * désactive la sécurisation par défaut d'Actuator : sans cette chaîne
     * dédiée (évaluée en premier, {@code @Order(1)}), la chaîne principale
     * ci-dessous s'appliquerait aussi aux requêtes reçues sur
     * {@code management.server.port}, qui n'envoient pourtant aucun jeton
     * (sondes kubelet, scraping Prometheus). L'isolation réseau du port de
     * gestion (jamais exposé par l'Ingress) tient lieu de protection ici.
     */
    @Bean
    @Order(1)
    SecurityFilterChain actuatorFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(EndpointRequest.toAnyEndpoint())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain filterChain(HttpSecurity http, JwtTokenProvider jwtTokenProvider,
                                     AuthRateLimitFilter authRateLimitFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(ROUTES_PUBLIQUES).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, ex) -> ecrireProblemDetail(
                                response, HttpStatus.UNAUTHORIZED, "AUTHENTIFICATION_REQUISE",
                                "Authentification requise."))
                        .accessDeniedHandler((request, response, ex) -> ecrireProblemDetail(
                                response, HttpStatus.FORBIDDEN, "ACCES_REFUSE",
                                "Vous n'avez pas les droits nécessaires pour cette action.")))
                .addFilterBefore(authRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSourceBean() {
        return corsConfigurationSource();
    }

    /**
     * §16.2 (durcissement) — origines explicitement listées via
     * {@code app.cors-allowed-origins} (par défaut, l'URL du frontend),
     * jamais {@code *} : un joker est de toute façon rejeté par les
     * navigateurs dès lors que {@code allowCredentials(true)} est actif.
     */
    private UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(originesAutorisees);
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private void ecrireProblemDetail(HttpServletResponse response, HttpStatus statut, String code, String message)
            throws IOException {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(statut, message);
        problemDetail.setProperty("code", code);

        response.setStatus(statut.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(problemDetail));
    }
}
