package com.example.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity(securedEnabled = true)
public class AuthServiceApplication {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        SpringApplication.run(AuthServiceApplication.class, args);
        System.out.println("Hello World!");
    }

}
