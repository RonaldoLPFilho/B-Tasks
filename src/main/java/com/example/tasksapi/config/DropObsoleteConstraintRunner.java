package com.example.tasksapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Remove constraints obsoletas da tabela task.
 * - ux_task_user_sort: substituída por ux_task_tab_sort na feature Tabs
 * - ux_task_tab_sort: substituída por ux_task_section_sort na feature Sections
 * Hibernate ddl-auto=update não remove constraints antigas.
 */
@Component
@Order(0)
public class DropObsoleteConstraintRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DropObsoleteConstraintRunner.class);
    private static final String OLD_CONSTRAINT_USER = "ux_task_user_sort";
    private static final String OLD_CONSTRAINT_TAB = "ux_task_tab_sort";

    private final JdbcTemplate jdbcTemplate;

    public DropObsoleteConstraintRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        dropConstraintIfExists(OLD_CONSTRAINT_USER);
        dropConstraintIfExists(OLD_CONSTRAINT_TAB);
    }

    private void dropConstraintIfExists(String constraintName) {
        try {
            jdbcTemplate.execute("ALTER TABLE task DROP CONSTRAINT IF EXISTS " + constraintName);
            log.info("Dropped obsolete constraint {} if it existed", constraintName);
        } catch (Exception e) {
            log.warn("Could not drop constraint {}: {}", constraintName, e.getMessage());
        }
    }
}
