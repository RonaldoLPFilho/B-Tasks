package com.example.tasksapi.config;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Value("${FRONT_URL:http://localhost:18880}")
    private String frontUrl;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        Set<String> allowedOrigins = new LinkedHashSet<>();
        allowedOrigins.add(frontUrl);
        allowedOrigins.add("http://localhost:18880");
        allowedOrigins.add("http://127.0.0.1:18880");
        allowedOrigins.add("http://localhost:5173");
        allowedOrigins.add("http://localhost:5174");
        allowedOrigins.add("http://127.0.0.1:5173");
        allowedOrigins.add("http://127.0.0.1:5174");
        allowedOrigins.add("http://192.168.15.8:5173");

        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigins.toArray(String[]::new))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
