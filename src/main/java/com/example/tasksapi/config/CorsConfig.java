package com.example.tasksapi.config;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Value("${FRONT_URL:http://localhost:18880}")
    private String frontUrl;

    @Value("${TASKS_CORS_ALLOWED_ORIGINS:}")
    private String configuredAllowedOrigins;

    @Value("${TASKS_CORS_ALLOWED_ORIGIN_PATTERNS:http://localhost:*,http://127.0.0.1:*,http://192.168.*.*:*,http://10.*.*.*:*,http://172.*.*.*:*}")
    private String configuredAllowedOriginPatterns;

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
        addCsvValues(allowedOrigins, configuredAllowedOrigins);

        Set<String> allowedOriginPatterns = new LinkedHashSet<>();
        addCsvValues(allowedOriginPatterns, configuredAllowedOriginPatterns);

        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                CorsRegistration registration = registry.addMapping("/**")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*")
                        .allowCredentials(true);

                if (!allowedOrigins.isEmpty()) {
                    registration.allowedOrigins(allowedOrigins.toArray(String[]::new));
                }

                if (!allowedOriginPatterns.isEmpty()) {
                    registration.allowedOriginPatterns(allowedOriginPatterns.toArray(String[]::new));
                }
            }
        };
    }

    private void addCsvValues(Set<String> target, String csvValues) {
        if (!StringUtils.hasText(csvValues)) {
            return;
        }

        for (String value : csvValues.split(",")) {
            if (StringUtils.hasText(value)) {
                target.add(value.trim());
            }
        }
    }
}
