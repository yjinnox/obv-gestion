package com.obvgestion.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.obvgestion")
public class ObvGestionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObvGestionApplication.class, args);
    }
}
