package com.example.tasksapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class CategorySchemaMigrationRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(CategorySchemaMigrationRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public CategorySchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute("ALTER TABLE category ADD COLUMN IF NOT EXISTS is_default BOOLEAN NOT NULL DEFAULT FALSE");
            log.info("Ensured category.is_default column exists");
        } catch (Exception e) {
            log.warn("Could not ensure category.is_default column: {}", e.getMessage());
        }
    }
}
