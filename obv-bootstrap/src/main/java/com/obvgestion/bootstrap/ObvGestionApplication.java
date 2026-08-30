package com.obvgestion.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * {@code @SpringBootApplication(scanBasePackages = ...)} ne pilote que la
 * découverte des {@code @Component} : l'inférence de paquet utilisée par
 * {@code @EnableAutoConfiguration} pour localiser les entités JPA reste le
 * paquet de cette classe, d'où {@code @EntityScan} explicite vers
 * {@code obv-domain} (les repositories Spring Data sont déclarés séparément
 * via {@code @EnableJpaRepositories} dans obv-infrastructure).
 */
@SpringBootApplication(scanBasePackages = "com.obvgestion", exclude = UserDetailsServiceAutoConfiguration.class)
@EntityScan(basePackages = "com.obvgestion.domain")
@EnableScheduling
public class ObvGestionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObvGestionApplication.class, args);
    }
}
