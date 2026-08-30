package com.obvgestion.infrastructure.persistence;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * {@code @SpringBootApplication(scanBasePackages = "com.obvgestion")} (obv-bootstrap) couvre
 * la découverte des {@code @Component}, mais pas l'inférence de paquet de
 * {@code @EnableAutoConfiguration} (base package = paquet de la classe
 * annotée), dont dépend la découverte des repositories Spring Data. D'où
 * la déclaration explicite ici.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableJpaRepositories(basePackages = "com.obvgestion.infrastructure.persistence")
class JpaAuditingConfig {
}
